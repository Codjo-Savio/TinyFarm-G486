package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.StockRepository;
import com.api.tinyfarm.repository.TransactionRepository;
import com.api.tinyfarm.repository.UserRepository;
import com.api.tinyfarm.service.UserService;
import com.sun.jdi.connect.TransportTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.object.UpdatableSqlQuery;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    private TransactionRepository transactionRepository;

    private UserRepository userRepository;

    private UserService userService;

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
        // Récupération du stock de l'utilisateur qui achète
        List<Stock> stock = findByUser(transaction.get().getSeller());

        // Gestion du stock de l'utilisateur qui achète en fonction de la transaction

        int quantity = transaction.get().getQuantity();

        // Gestion des écus de l'utilisateur qui achète en fonction de la transaction

        float ecuLose = transaction.get().getTotalPrice();

        // On retire la quantité d'objet vendu dans le stock en l'updatant avec le nouveau stock.

        Stock updatedStock = new Stock();
        updatedStock.setId(stock.get(0).getId());
        updatedStock.setUserId(stock.get(0).getUserId());
        updatedStock.setProductId(stock.get(0).getProductId());
        updatedStock.setQuantity((stock.get(0).getQuantity()) + quantity); // On retire la quantité de l'achat.
        updatedStock.setCollectible(stock.get(0).getCollectible());

        // On retire la quantité d'objet vendu dans le stock en l'updatant avec le nouveau stock.

        Optional<User> buyer = userRepository.findById(
            stock.get(0).getUserId()
        );

        // Création du User modifié :

        User updatedUser = new User();
        updatedUser.setId(buyer.get().getId());
        updatedUser.setName(buyer.get().getName());
        updatedUser.setEmail(buyer.get().getEmail());
        updatedUser.setEcus(buyer.get().getEcus() - ecuLose);
        updatedUser.setGender(buyer.get().getGender());
        updatedUser.setHibernation(buyer.get().getHibernation());

        // Update

        userService.update(buyer.get().getId(), updatedUser);
        update(
            buyer.get().getId(),
            transaction.get().getProduct(),
            updatedStock
        );
    }
}
