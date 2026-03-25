package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.repository.MarketRepository;
import com.api.tinyfarm.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MarketService {

    private final MarketRepository marketRepository;

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public Market findById(Long uid) {
        return marketRepository
            .findById(uid)
            .orElseThrow(() ->
                new RuntimeException("Marché introuvable : " + uid)
            );
    }

    public Market findByProductId(Long productID) {
        return marketRepository
            .findByProductId(productID)
            .orElseThrow(() ->
                new RuntimeException("Marché introuvable : " + productID)
            );
    }

    public Market findByPrice(float price) {
        return marketRepository
            .findByPrice(price)
            .orElseThrow(() ->
                new RuntimeException("Marché introuvable : " + price)
            );
    }

    public Market create(Market market) {
        return marketRepository.save(market);
    }

    public Market update(Long id, Market modifiedMarket) {
        Market existing = findById(id);
        existing.setUserId(modifiedMarket.getUserId());
        existing.setProductId(modifiedMarket.getProductId());
        existing.setPrice(modifiedMarket.getPrice());
        return marketRepository.save(existing);
    }

    public void deleteProductById(Long userId, Long productId) {
        try {
            marketRepository.deleteByMarketIdUserIdAndMarketIdProductId(
                userId,
                productId
            );
        } catch (Exception e) {
            throw new RuntimeException(
                "Impossible de retirer le produit du marché : " + e.getMessage()
            );
        }
    }

    public void deleteByID(Long uid) {
        marketRepository.deleteById(uid);
    }

    public void deleteAll() {
        marketRepository.deleteAll();
    }
}
