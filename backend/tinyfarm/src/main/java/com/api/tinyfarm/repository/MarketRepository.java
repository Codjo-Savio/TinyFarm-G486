package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.MarketID;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepository extends JpaRepository<Market, MarketID> {
    Optional<Market> findById(MarketID id);
    Optional<Market> findByUserId(Long userId);
    Optional<Market> findByProductId(Long productId);
    @Query("select m from Market m where m.unitPrice = :price")
    Optional<Market> findByPrice(@Param("price") float price);
    Optional<Market> findByQuantity(int quantity);
    void deleteByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserId(Long userId);
}
