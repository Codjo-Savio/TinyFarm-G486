package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, StockId> {
    Optional<Stock> findById(StockId id);

    Optional<Stock> findByIdUserId(Long userId);
}
