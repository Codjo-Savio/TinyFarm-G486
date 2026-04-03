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
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        Transaction saved = transactionRepository.save(transaction);
        Optional<Transaction> found = transactionRepository.findById(
            saved.getId()
        );

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void shouldFindByBuyer() {
        Transaction transaction = new Transaction();
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionRepository.save(transaction);
        Optional<Transaction> found = transactionRepository.findByBuyer(9L);

        assertTrue(found.isPresent());
        assertEquals(9L, found.get().getBuyer());
    }

    @Test
    void shouldFindBySeller() {
        Transaction transaction = new Transaction();
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionRepository.save(transaction);
        Optional<Transaction> found = transactionRepository.findBySeller(4L);

        assertTrue(found.isPresent());
        assertEquals(4L, found.get().getSeller());
    }

    @Test
    void shouldFindByProduct() {
        Transaction transaction = new Transaction();
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
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        Transaction saved = transactionRepository.save(transaction);
        transactionRepository.deleteById(saved.getId());

        Optional<Transaction> found = transactionRepository.findById(
            saved.getId()
        );

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAllTransaction() {
        Transaction firstT = new Transaction();
        firstT.setSeller(4L);
        firstT.setBuyer(9L);
        firstT.setProduct(61L);
        firstT.setQuantity(10);
        firstT.setTotalPrice(150.0F);

        Transaction secondT = new Transaction();
        secondT.setSeller(10L);
        secondT.setBuyer(22L);
        secondT.setProduct(12L);
        secondT.setQuantity(20);
        secondT.setTotalPrice(250.0F);

        transactionRepository.save(firstT);
        transactionRepository.save(secondT);

        List<Transaction> founds = transactionRepository.findAll();

        assertEquals(2, founds.size());
    }
}
