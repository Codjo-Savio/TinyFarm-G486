package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.MarketID;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.MarketRepository;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketService {

    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private TradeService tradeService;

    public Market findByUserId(Long uid) {
        return marketRepository
            .findByUserId(uid)
            .orElseThrow(() ->
                new RuntimeException("Marché introuvable : " + uid)
            );
    }

    public Market findById(MarketID id) {
        return marketRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Marché introuvable pour l'utilisateur : " + id.getUserId()
                         + " ou pour le produit : " + id.getProductID())
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

    public Market findByQuantity(int quantity) {
        return marketRepository
            .findByQuantity(quantity)
            .orElseThrow(() ->
                new RuntimeException("Marché introuvable : " + quantity)
            );
    }

    public Market create(Market market) {
        syncMarketId(market);
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (
            authentication != null &&
            authentication.getPrincipal() instanceof User currentUser
        ) {
            market.setUserId(currentUser.getId());
        }
        return marketRepository.save(market);
    }

    public List<Market> findAll() {
        return marketRepository.findAll();
    }

    public Market update(Long uid, Market modifiedMarket) {
        Market existing = findByUserId(uid);
        existing.setUserId(modifiedMarket.getUserId());
        existing.setProductId(modifiedMarket.getProductId());
        existing.setUnitPrice(modifiedMarket.getUnitPrice());
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

    private void syncMarketId(Market market) {
        if (market.getUserId() == null || market.getProductId() == null) {
            return;
        }
        market.setMarketId(
            new MarketID(market.getUserId(), market.getProductId())
        );
    }

    public void buyFromMarket(
        Long buyerId,
        Long sellerId,
        Long productId,
        Integer quantity
    ) {
        MarketID marketId = new MarketID(sellerId, productId);
        Market market = findById(marketId);

        syncMarketId(market);
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (
                authentication != null &&
                        authentication.getPrincipal() instanceof User currentUser
        ) {
            buyerId = currentUser.getId(); // the buyer is the one who is connected on his application
        }

        tradeService.buy(sellerId, buyerId, productId, quantity, market.getUnitPrice());
    }
}
