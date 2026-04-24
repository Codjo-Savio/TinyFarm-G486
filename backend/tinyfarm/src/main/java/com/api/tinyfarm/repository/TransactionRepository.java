package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Transaction;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository
    extends JpaRepository<Transaction, Long>
{
    Optional<Transaction> findByBuyer(Long buyer);
    Optional<Transaction> findBySeller(Long seller);
    Optional<Transaction> findByProduct(Long product);
}
