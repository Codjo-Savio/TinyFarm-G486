package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Transaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{
    Optional<Transaction> findById(Long id);
    Optional<Transaction> findByBuyerId(Long buyer);
    Optional<Transaction> findBySellerId(Long seller);
    Optional<List<Transaction>> findByProduct(Long product);
    void deleteAll();
    void deleteById(Long id);
}
