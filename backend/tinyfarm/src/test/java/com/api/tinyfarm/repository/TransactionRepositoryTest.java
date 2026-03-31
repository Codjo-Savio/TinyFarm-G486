package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.tinyfarm.model.Transaction;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldSaveTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSeller(1L);
        transaction.setBuyer(2L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        Transaction saved = transactionRepository.save(transaction);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getSeller());
        assertEquals(2L, saved.getBuyer());
        assertEquals(61L, saved.getProduct());
        assertEquals(10, saved.getQuantity());
        assertEquals(150.0F, saved.getTotalPrice());
    }

    @Test
    void shouldFindByTransactionId() {
        Transaction transaction = new Transaction();
        transaction.setId(2L);
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionRepository.save(transaction);
        Optional<Transaction> found = transactionRepository.findById(2L);

        assertTrue(found.isPresent());
        assertEquals(2L, found.get().getId());
    }

    @Test
    void shouldFindByBuyerId() {
        Transaction transaction = new Transaction();
        transaction.setId(2L);
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionRepository.save(transaction);
        Optional<Transaction> found = transactionRepository.findByBuyerId(9L);

        assertTrue(found.isPresent());
        assertEquals(9L, found.get().getBuyer());
    }

    @Test
    void shouldFindBySellerId() {
        Transaction transaction = new Transaction();
        transaction.setId(2L);
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionRepository.save(transaction);
        Optional<Transaction> found = transactionRepository.findBySellerId(4L);

        assertTrue(found.isPresent());
        assertEquals(4L, found.get().getSeller());
    }

    @Test
    void shouldFindByProduct() {
        Transaction transaction = new Transaction();
        transaction.setId(2L);
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionRepository.save(transaction);
        Optional<Transaction> found = transactionRepository.findByProduct(61L);

        assertTrue(found.isPresent());
        assertEquals(61L, found.get().getProduct());
    }

    @Test
    void shouldDeleteById() {
        Transaction transaction = new Transaction();
        transaction.setId(2L);
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionRepository.save(transaction);
        transactionRepository.deleteById(2L);

        Optional<Transaction> found = transactionRepository.findById(2L);

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAllTransaction() {
        Transaction firstT = new Transaction();
        firstT.setId(2L);
        firstT.setSeller(4L);
        firstT.setBuyer(9L);
        firstT.setProduct(61L);
        firstT.setQuantity(10);
        firstT.setTotalPrice(150.0F);

        transactionRepository.save(firstT);

        Transaction secondT = new Transaction();
        secondT.setId(6L);
        secondT.setSeller(10L);
        secondT.setBuyer(22L);
        secondT.setProduct(12L);
        secondT.setQuantity(20);
        secondT.setTotalPrice(250.0F);

        transactionRepository.save(secondT);

        List<Transaction> found = transactionRepository.findAll();

        assertEquals(2, found.size());
    }
}
