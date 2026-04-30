package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.StockRepository;
import com.api.tinyfarm.repository.TransactionRepository;
import com.api.tinyfarm.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradeService {
    private static final float AUTHORIZED_OVERDRAFT_FLOOR = -1500f;

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void buy(Long sellerId,
                     Long buyerId,
                     Long productId,
                     Integer quantity,
                     Float price) {
        executeTrade(sellerId, buyerId, productId, quantity, price);
    }

    private void executeTrade(Long sellerId,
                              Long buyerId,
                              Long productId,
                              Integer quantity,
                              Float price) {

        User seller = getUserOrThrow(sellerId, "Vendeur introuvable");
        User buyer = getUserOrThrow(buyerId, "Acheteur introuvable");

        float totalPrice = applyMoneyAndPurchaseLimit(seller, buyer, quantity, price);

        addToBuyerStock(buyerId, productId, quantity);

        saveTransaction(sellerId, buyerId, productId, quantity, totalPrice);

    }

    private void addToBuyerStock(Long buyerId, Long productId, Integer quantity) {
        StockId buyerStockId = new StockId(buyerId, productId);
        Optional<Stock> buyerStock = stockRepository.findById(buyerStockId);
        if (buyerStock.isPresent()) {
            Stock existingBuyerStock = buyerStock.get();
            existingBuyerStock.setQuantity(existingBuyerStock.getQuantity() + quantity);
            stockRepository.save(existingBuyerStock);
            return;
        }

        Stock newBuyerStock = new Stock();
        newBuyerStock.setId(buyerStockId);
        newBuyerStock.setQuantity(quantity);
        stockRepository.save(newBuyerStock);
    }

    private User getUserOrThrow(Long userId, String errorMessage) {
        return userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException(errorMessage));
    }

    private float applyMoneyAndPurchaseLimit(User seller, User buyer, Integer quantity, Float unitPrice) {
        // Daily purchase cap is global and is consumed by both market and cooperative purchases.
        float totalPrice = unitPrice * quantity;
        if (buyer.getEcus() - totalPrice < AUTHORIZED_OVERDRAFT_FLOOR) {
            throw new RuntimeException("Écus insuffisants pour effectuer l'achat");
        }

        int remainingPurchases = buyer.getRemainingPurchases() == null ? 12 : buyer.getRemainingPurchases();
        if (remainingPurchases <= 0) {
            throw new RuntimeException("Vous ne pouvez plus effectuer d'achat dans la journée");
        }

        buyer.setEcus(buyer.getEcus() - totalPrice);
        buyer.setRemainingPurchases(remainingPurchases - 1);
        seller.setEcus(seller.getEcus() + totalPrice);
        userRepository.save(buyer);
        userRepository.save(seller);
        return totalPrice;
    }

    private void saveTransaction(Long sellerId, Long buyerId, Long productId, Integer quantity, float totalPrice) {
        Transaction transaction = new Transaction();
        transaction.setBuyer(buyerId);
        transaction.setSeller(sellerId);
        transaction.setProduct(productId);
        transaction.setQuantity(quantity);
        transaction.setTotalPrice(totalPrice);
        transactionRepository.save(transaction);
    }
}
