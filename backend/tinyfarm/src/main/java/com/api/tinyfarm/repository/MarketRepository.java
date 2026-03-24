package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {

    Optional<Market> findById(Long uid);
    Optional<Market> findByProduct(Long productID);
    Optional<Market> findByPrice(float price);
    void deleteById(Long uid);
}
