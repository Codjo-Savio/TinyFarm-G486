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

    public List<Market> findByUserId(Long uid) {
        return marketRepository
            .findByMarketIdUserId(uid);
    }

    public Market findById(MarketID id) {
        return marketRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Marché introuvable pour l'utilisateur : " + id.getUserId()
                         + " ou pour le produit : " + id.getProductId())
                );
    }

    public Market findByProductId(Long productID) {
        return marketRepository
            .findByMarketIdProductId(productID)
            .orElseThrow(() ->
                new RuntimeException("Marché introuvable : " + productID)
            );
    }

    public List<Market> findByPrice(float price) {
        return marketRepository
            .findByPrice(price);
    }

    public List<Market> findByQuantity(int quantity) {
        return marketRepository
            .findByQuantity(quantity);
    }

    public Market create(Market market) {
        if (market == null) {
            throw new IllegalArgumentException("Offre marché manquante");
        }
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (
            authentication != null &&
            authentication.getPrincipal() instanceof User currentUser
        ) {
            market.setUserId(currentUser.getId());
        }
        if (market.getUserId() == null) {
            throw new IllegalArgumentException("userId manquant pour le marché");
        }
        if (market.getProductId() == null) {
            throw new IllegalArgumentException("productId manquant pour le marché");
        }
        if (market.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantité marché invalide");
        }
        if (market.getUnitPrice() == null || market.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Prix unitaire marché invalide");
        }
        syncMarketId(market);
        if (marketRepository.existsById(market.getMarketId())) {
            throw new IllegalArgumentException("Offre marché déjà existante pour cet utilisateur / produit");
        }
        return marketRepository.save(market);
    }

    public List<Market> findAll() {
        return marketRepository.findAll();
    }

    public List<Market> findAllExceptOnesOfTheConnectedUser(Long id){
        return marketRepository.findByMarketIdUserIdNot(id);
    }

    public Market update(Long uid, Long productId, Market modifiedMarket) {
        MarketID marketID = new MarketID(uid, productId);
        Market existing = findById(marketID);
        existing.setUserId(modifiedMarket.getUserId());
        existing.setProductId(modifiedMarket.getProductId());
        existing.setUnitPrice(modifiedMarket.getUnitPrice());
        syncMarketId(existing);
        return marketRepository.save(existing);
    }

    @Transactional
    public void deleteProductById(Long userId, Long productId) {
        try {
            marketRepository.deleteByMarketIdUserIdAndMarketIdProductId(userId, productId);
        } catch (Exception e) {
            throw new RuntimeException(
                "Impossible de retirer le produit du marché : " + e.getMessage()
            );
        }
    }

    @Transactional
    public void deleteByID(Long uid) {
        marketRepository.deleteByMarketIdUserId(uid);
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
