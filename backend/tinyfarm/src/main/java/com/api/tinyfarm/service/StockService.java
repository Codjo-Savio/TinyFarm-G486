package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.StockRepository;
import com.api.tinyfarm.repository.TransactionRepository;
import com.api.tinyfarm.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public StockService(
        StockRepository stockRepository,
        TransactionRepository transactionRepository,
        UserRepository userRepository,
        UserService userService
    ) {
        this.stockRepository = stockRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    public Stock findById(Long userId, Long productId) {
        StockId id = new StockId(userId, productId);
        return stockRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException(
                    "Stock introuvable : " + userId + "/" + productId
                )
            );
    }

    public List<Stock> findByUser(Long userId) {
        return stockRepository
            .findAll()
            .stream()
            .filter(stock -> stock.getId().getUid().equals(userId))
            .collect(Collectors.toList());
    }

    public List<Stock> findByProduct(Long productId) {
        return stockRepository
            .findAll()
            .stream()
            .filter(stock -> stock.getId().getProductID().equals(productId))
            .collect(Collectors.toList());
    }

    public Stock create(Stock stock) throws Exception {
        if (stock == null || stock.getId() == null) {
            throw new IllegalArgumentException("Stock invalide");
        }

        StockId id = stock.getId();
        Long userId = id.getUid();
        Long productId = id.getProductID();
        if (userId == null || productId == null) {
            throw new IllegalArgumentException(
                "Clé composite manquante dans stock"
            );
        }

        if (stockRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "Stock déjà existant pour cet utilisateur / produit"
            );
        }

        return stockRepository.save(stock);
    }

    public Stock update(Long userId, Long productId, Stock stock) {
        Stock existing = findById(userId, productId);
        if (stock.getQuantity() != null) {
            existing.setQuantity(stock.getQuantity());
        }
        if (stock.getCollectible() != null) {
            existing.setCollectible(stock.getCollectible());
        }
        return stockRepository.save(existing);
    }

    public void deleteAll() {
        stockRepository.deleteAll();
    }

    public void delete(Long userId, Long productId) {
        StockId id = new StockId(userId, productId);
        stockRepository.deleteById(id);
    }

    public void deleteByUser(Long userId) {
        findByUser(userId).forEach(s -> stockRepository.deleteById(s.getId()));
    }

    public void deleteByProduct(Long productId) {
        findByProduct(productId).forEach(s ->
            stockRepository.deleteById(s.getId())
        );
    }

    public void sell(Long tid) {
        // Récupération de la transaction d'achat
        Optional<Transaction> transaction = transactionRepository.findById(tid);
        // Récupération du stock de l'utilisateur qui vend
        List<Stock> stock = findByUser(transaction.get().getSeller());

        // Gestion du stock de l'utilisateur qui vend en fonction de la transaction

        int quantity = transaction.get().getQuantity();

        // Gestion des écus de l'utilisateur qui achète en fonction de la transaction

        float ecuGain = transaction.get().getTotalPrice();

        // On retire la quantité d'objet vendu dans le stock en l'updatant avec le nouveau stock.

        Stock updatedStock = new Stock();
        updatedStock.setId(stock.get(0).getId());
        updatedStock.setUserId(stock.get(0).getUserId());
        updatedStock.setProductId(stock.get(0).getProductId());
        updatedStock.setQuantity((stock.get(0).getQuantity()) - quantity); // On retire la quantité de la vente.
        updatedStock.setCollectible(stock.get(0).getCollectible());

        Optional<User> seller = userRepository.findById(
            stock.get(0).getUserId()
        );

        // Création du User modifié :

        User updatedUser = new User();
        updatedUser.setId(seller.get().getId());
        updatedUser.setName(seller.get().getName());
        updatedUser.setEmail(seller.get().getEmail());
        updatedUser.setEcus(seller.get().getEcus() + ecuGain);
        updatedUser.setGender(seller.get().getGender());
        updatedUser.setHibernation(seller.get().getHibernation());

        // Update

        userService.update(seller.get().getId(), updatedUser);
        update(
            seller.get().getId(),
            transaction.get().getProduct(),
            updatedStock
        );
    }

    public void buy(Long tid) {
        // Récupération de la transaction d'achat
        Optional<Transaction> transaction = transactionRepository.findById(tid);
        if (!transaction.isPresent()) {
            throw new RuntimeException("Transaction non trouvée");
        }

        Transaction trans = transaction.get();
        Long sellerId = trans.getSeller();
        Long buyerId = trans.getBuyer();
        Long productId = trans.getProduct();
        int quantity = trans.getQuantity();
        float totalPrice = trans.getTotalPrice();

        // Récupération du stock du vendeur pour ce produit
        List<Stock> sellerStocks = findByUser(sellerId);
        Stock sellerStock = sellerStocks
            .stream()
            .filter(s -> s.getId().getProductID().equals(productId))
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("Stock du vendeur non trouvé")
            );

        // Mise à jour du stock du vendeur (diminution de la quantité vendue)
        Stock updatedSellerStock = new Stock();
        updatedSellerStock.setId(sellerStock.getId());
        updatedSellerStock.setUserId(sellerStock.getUserId());
        updatedSellerStock.setProductId(sellerStock.getProductId());
        updatedSellerStock.setQuantity(sellerStock.getQuantity() - quantity);
        updatedSellerStock.setCollectible(sellerStock.getCollectible());

        // Mise à jour du stock de l'acheteur
        List<Stock> buyerStocks = findByUser(buyerId);
        Stock buyerStock = buyerStocks
            .stream()
            .filter(s -> s.getId().getProductID().equals(productId))
            .findFirst()
            .orElse(null);

        if (buyerStock == null) {
            // Créer un nouveau stock pour l'acheteur
            buyerStock = new Stock();
            buyerStock.setId(new StockId(buyerId, productId));
            buyerStock.setQuantity(quantity);
            buyerStock.setCollectible(sellerStock.getCollectible());
            stockRepository.save(buyerStock);
        } else {
            // Augmenter le stock existant
            Stock updatedBuyerStock = new Stock();
            updatedBuyerStock.setId(buyerStock.getId());
            updatedBuyerStock.setUserId(buyerStock.getUserId());
            updatedBuyerStock.setProductId(buyerStock.getProductId());
            updatedBuyerStock.setQuantity(buyerStock.getQuantity() + quantity);
            updatedBuyerStock.setCollectible(buyerStock.getCollectible());
            update(buyerId, productId, updatedBuyerStock);
        }

        // Mise à jour des écus
        Optional<User> seller = userRepository.findById(sellerId);
        Optional<User> buyer = userRepository.findById(buyerId);

        if (seller.isPresent() && buyer.isPresent()) {
            // Mise à jour du vendeur (gain d'écus)
            User updatedSeller = new User();
            updatedSeller.setId(seller.get().getId());
            updatedSeller.setName(seller.get().getName());
            updatedSeller.setEmail(seller.get().getEmail());
            updatedSeller.setEcus(seller.get().getEcus() + totalPrice);
            updatedSeller.setGender(seller.get().getGender());
            updatedSeller.setHibernation(seller.get().getHibernation());

            // Mise à jour de l'acheteur (perte d'écus)
            User updatedBuyer = new User();
            updatedBuyer.setId(buyer.get().getId());
            updatedBuyer.setName(buyer.get().getName());
            updatedBuyer.setEmail(buyer.get().getEmail());
            updatedBuyer.setEcus(buyer.get().getEcus() - totalPrice);
            updatedBuyer.setGender(buyer.get().getGender());
            updatedBuyer.setHibernation(buyer.get().getHibernation());

            userService.update(sellerId, updatedSeller);
            userService.update(buyerId, updatedBuyer);
        }

        // Mise à jour du stock du vendeur
        update(sellerId, productId, updatedSellerStock);
    }
}
