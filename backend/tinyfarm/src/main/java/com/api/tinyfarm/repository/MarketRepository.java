package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Market;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    Optional<Market> findByUserId(Long userId);
    Optional<Market> findByProductId(Long productId);
    Optional<Market> findByPrice(float price);
    void deleteByUserIdAndProductId(Long userId, Long productId);
    void deleteById(Long userId);
}
