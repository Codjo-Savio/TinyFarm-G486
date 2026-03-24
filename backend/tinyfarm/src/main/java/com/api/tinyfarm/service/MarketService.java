package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.repository.MarketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {

    private final MarketRepository marketRepository;

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public Market findById(Long uid) {
        return marketRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("Marché introuvable : " + uid));
    }

    public Market findByProduct(Long productID) {
        return marketRepository.findByProduct(productID)
                .orElseThrow(() -> new RuntimeException("Marché introuvable : " + productID));
    }

    public Market findByPrice(float price) {
        return marketRepository.findByPrice(price)
                .orElseThrow(() -> new RuntimeException("Marché introuvable : " + price));
    }

    public Market create(Market market) {
        return marketRepository.save(market);
    }

    public Market update(Long id, Market modifiedMarket) {
        Market existing = findById(id);
        existing.setMarketUid(modifiedMarket.getMarketUid());
        existing.setProductId(modifiedMarket.getProductId());
        existing.setPrice(modifiedMarket.getPrice());
        return marketRepository.save(existing);
    }

    public void deleteByID(Long uid) {
        MarketRepository.deleteById(uid);
    }
}
