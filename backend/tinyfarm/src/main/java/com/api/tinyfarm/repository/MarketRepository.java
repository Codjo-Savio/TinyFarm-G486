package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Market;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    Optional<Market> findByUserId(Long uid);
    Optional<Market> findByProduct(Long productID);
    Optional<Market> findByPrice(float price);
    void deleteById(Long uid);
}
