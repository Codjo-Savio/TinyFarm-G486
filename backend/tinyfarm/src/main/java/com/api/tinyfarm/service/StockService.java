package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.repository.StockRepository;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    public Stock findById(Long userId, Long productId){
        StockId id = new StockId(userId, productId);
        return stockRepository.findById(id).get();
    }

    public List<Stock> findByUser(Long userId) {
        return stockRepository.findAll().stream()
            .filter(stock -> stock.getId().getUid().equals(userId))
            .collect(Collectors.toList());
    }

    public List<Stock> findByProduct(Long productId) {
        return stockRepository.findAll().stream()
            .filter(stock -> stock.getId().getProductID().equals(productId))
            .collect(Collectors.toList());
    }

    public Stock create(Stock stock) throws Exception{
        if (stock == null || stock.getId() == null) {
            throw new IllegalArgumentException("Stock invalide");
        }

        Long userId = stock.getUserId();
        Long productId = stock.getProductId();
        if (userId == null || productId == null) {
            throw new IllegalArgumentException("Clé composite manquante dans stock");
        }

        if(stockRepository.existsById(stock.getId())){
            throw new Exception(("Ce stock existe déjà"));
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

    public void delete(Long userId, Long productId) {
        StockId id = new StockId(userId, productId);
        stockRepository.deleteById(id);
    }

    public void deleteByUser(Long userId) {
        findByUser(userId).forEach(s -> stockRepository.deleteById(s.getId()));
    }

    public void deleteByProduct(Long productId) {
        findByProduct(productId).forEach(s -> stockRepository.deleteById(s.getId()));
    }
}
