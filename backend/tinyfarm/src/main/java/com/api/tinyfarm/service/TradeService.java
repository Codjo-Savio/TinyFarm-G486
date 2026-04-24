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


        // handling the seller stock
        StockId sellerStockId = new StockId(sellerId, productId);
        Stock sellerStock = stockRepository
            .findById(sellerStockId)
            .orElseThrow(() ->
                new RuntimeException("Stock du vendeur non trouvé")
            );
        if (sellerStock.getQuantity() < quantity) {
            throw new RuntimeException("Quantité insuffisante dans le stock vendeur");
        }
        sellerStock.setQuantity(sellerStock.getQuantity() - quantity);
        stockRepository.save(sellerStock);

        // handling the buyer stock
        StockId buyerStockId = new StockId(buyerId, productId);
        Optional<Stock> buyerStock = stockRepository.findById(buyerStockId);
        if (buyerStock.isPresent()) {
            Stock existingBuyerStock = buyerStock.get();
            existingBuyerStock.setQuantity(
                existingBuyerStock.getQuantity() + quantity
            );
            stockRepository.save(existingBuyerStock);
        } else {
            Stock newBuyerStock = new Stock();
            newBuyerStock.setId(buyerStockId);
            newBuyerStock.setQuantity(quantity);
            newBuyerStock.setCollectible(sellerStock.getCollectible());
            stockRepository.save(newBuyerStock);
        }

        // Ecus handling
        User seller = userRepository
            .findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Vendeur introuvable"));
        User buyer = userRepository
            .findById(buyerId)
            .orElseThrow(() -> new RuntimeException("Acheteur introuvable"));

        float totalPrice = price*quantity;
        if (buyer.getEcus() < totalPrice) {
            throw new RuntimeException("Écus insuffisants pour effectuer l'achat");
        }

        int remainingPurchases = buyer.getRemainingPurchases() == null
            ? 12
            : buyer.getRemainingPurchases();
        if (remainingPurchases <= 0) {
            throw new RuntimeException("Vous ne pouvez plus effectuer d'achat dans la journée");
        }

        buyer.setEcus(buyer.getEcus() - totalPrice);
        buyer.setRemainingPurchases(remainingPurchases - 1);
        seller.setEcus(seller.getEcus() + totalPrice);
        userRepository.save(buyer);
        userRepository.save(seller);

        // creating the transaction
        Transaction transaction = new Transaction();
        transaction.setBuyer(buyerId);
        transaction.setSeller(sellerId);
        transaction.setProduct(productId);
        transaction.setQuantity(quantity);
        transaction.setTotalPrice(totalPrice);
        transactionRepository.save(transaction);

    }
}
