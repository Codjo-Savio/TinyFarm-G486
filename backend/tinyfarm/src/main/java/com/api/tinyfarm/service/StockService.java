package com.api.tinyfarm.service;

import com.api.tinyfarm.model.*;
import com.api.tinyfarm.repository.StockRepository;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private MarketService marketService;
   
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

    public Market publishToMarket(Long productId, Integer quantity, Float unitPrice){
            Market market = new Market();
            market.setProductId(productId);
            market.setQuantity(quantity);
            market.setUnitPrice(unitPrice);
            return marketService.create(market);
    }
}
