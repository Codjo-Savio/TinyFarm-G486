package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.MarketID;
import com.api.tinyfarm.repository.MarketRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketService {

    private final MarketRepository marketRepository;

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public Market findByUserId(Long uid) {
        return marketRepository
            .findByUserId(uid)
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
        syncMarketId(market);
        return marketRepository.save(market);
    }

    public List<Market> findAll() {
        return marketRepository.findAll();
    }

    public Market update(Long uid, Market modifiedMarket) {
        Market existing = findByUserId(uid);
        existing.setUserId(modifiedMarket.getUserId());
        existing.setProductId(modifiedMarket.getProductId());
        existing.setPrice(modifiedMarket.getPrice());
        syncMarketId(existing);
        return marketRepository.save(existing);
    }

    @Transactional
    public void deleteProductById(Long userId, Long productId) {
        try {
            marketRepository.deleteByUserIdAndProductId(userId, productId);
        } catch (Exception e) {
            throw new RuntimeException(
                "Impossible de retirer le produit du marché : " + e.getMessage()
            );
        }
    }

    @Transactional
    public void deleteByID(Long uid) {
        marketRepository.deleteByUserId(uid);
    }

    public void deleteAll() {
        marketRepository.deleteAll();
    }

    private void syncMarketId(Market market) {
        if (market.getUserId() == null || market.getProductId() == null) {
            return;
        }
        market.setMarketId(
            new MarketID(market.getUserId(), market.getProductId())
        );
    }
}
