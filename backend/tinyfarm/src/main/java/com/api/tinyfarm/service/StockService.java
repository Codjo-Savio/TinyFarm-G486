package com.api.tinyfarm.service;

import com.api.tinyfarm.model.*;
import com.api.tinyfarm.repository.StockRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public Stock findById(Long userId, Long productId){
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
            .filter(stock -> stock.getId().getUserId().equals(userId))
            .collect(Collectors.toList());
    }

    public List<Stock> findByProduct(Long productId) {
        return stockRepository
            .findAll()
            .stream()
            .filter(stock -> stock.getId().getProductId().equals(productId))
            .collect(Collectors.toList());
    }

    public Stock create(Stock stock) throws Exception {
        if (stock == null) {
            throw new IllegalArgumentException("Stock manquant");
        }
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (
                authentication != null &&
                        authentication.getPrincipal() instanceof User currentUser
        ) {
            stock.setUserId(currentUser.getId());
        }
        if (stock.getUserId() == null) {
            throw new IllegalArgumentException("userId manquant pour le stock");
        }
        if (stock.getProductId() == null) {
            throw new IllegalArgumentException("productId manquant pour le stock");
        }
        if (stock.getQuantity() == null || stock.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantité de stock invalide");
        }

        StockId id = new StockId(stock.getUserId(), stock.getProductId());
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
            if (stock.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantité invalide");
            }
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

    @Transactional
    public Market publishToMarket(Long productId, Integer quantity, Float unitPrice) {

        Long userId = getALong(quantity, unitPrice);

        Stock stock = findById(userId, productId);

        if (stock.getQuantity() < quantity) {
            throw new IllegalArgumentException("Stock insuffisant");
        }

        if (Objects.equals(stock.getQuantity(), quantity)) {
            delete(userId, productId);
        } else {
            stock.setQuantity(stock.getQuantity() - quantity);
            stockRepository.save(stock);
        }

        Market market = new Market();
        market.setUserId(userId);
        market.setProductId(productId);
        market.setQuantity(quantity);
        market.setUnitPrice(unitPrice);

        return marketService.create(market);
    }

    private static Long getALong(Integer quantity, Float unitPrice) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantité invalide");
        }

        if (unitPrice == null || unitPrice <= 0) {
            throw new IllegalArgumentException("Prix invalide");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication.getPrincipal() instanceof User currentUser)) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }

        return currentUser.getId();
    }
}
