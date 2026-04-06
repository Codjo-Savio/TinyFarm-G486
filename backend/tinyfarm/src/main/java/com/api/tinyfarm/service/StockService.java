package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.ProductRepository;
import com.api.tinyfarm.repository.StockRepository;
import com.api.tinyfarm.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public StockService(
        StockRepository stockRepository,
        UserRepository userRepository,
        ProductRepository productRepository
    ) {
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
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

    public Stock create(Stock stock) {
        if (stock == null || stock.getId() == null) {
            throw new IllegalArgumentException("Stock invalide");
        }

        Long userId = stock.getId().getUid();
        Long productId = stock.getId().getProductID();
        if (userId == null || productId == null) {
            throw new IllegalArgumentException(
                "Clé composite manquante dans stock"
            );
        }

        StockId id = new StockId(userId, productId);
        if (stockRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "Stock déjà existant pour cet utilisateur / produit"
            );
        }

        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                new RuntimeException("Utilisateur introuvable : " + userId)
            );
        Product product = productRepository
            .findById(productId)
            .orElseThrow(() ->
                new RuntimeException("Produit introuvable : " + productId)
            );

        stock.setUser(user);
        stock.setProduct(product);
        stock.setId(id);
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
}
