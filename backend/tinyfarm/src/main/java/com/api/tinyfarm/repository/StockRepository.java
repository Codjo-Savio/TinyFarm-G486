package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.Stock;

import java.util.Optional;

import com.api.tinyfarm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Stock.StockId> {
    Optional<Stock> findById(Stock.StockId id);

    Optional<Stock> findByProduct(Product product);

    Optional<Stock> findByUser(User user);

    Optional<Stock> findByQuantity(Integer quantity);

    Optional<Stock> findByCollectible(Boolean collectible);
}