package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.MarketID;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepository extends JpaRepository<Market, MarketID> {

    List<Market> findByMarketIdUserId(Long userId);

    Optional<Market> findByMarketIdProductId(Long productId);

    @Query("SELECT m FROM Market m WHERE m.unitPrice = :price")
    List<Market> findByPrice(@Param("price") float price);

    List<Market> findByQuantity(int quantity);

    List<Market> findByMarketIdUserIdNot(Long id);

    void deleteByMarketIdUserIdAndMarketIdProductId(Long userId, Long productId);

    void deleteByMarketIdUserId(Long userId);
}