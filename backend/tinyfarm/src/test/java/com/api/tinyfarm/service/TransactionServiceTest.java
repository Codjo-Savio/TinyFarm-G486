package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.api.tinyfarm.model.Transaction;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @BeforeEach
    void setup() {
        transactionService.deleteAll();
    }

    @Test
    void shouldSaveTransaction() {
        Transaction transaction = new Transaction();
        transaction.setSeller(1L);
        transaction.setBuyer(2L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        Transaction saved = transactionService.create(transaction);

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

        Transaction saved = transactionService.create(transaction);
        Transaction found = transactionService.findById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void shouldFindByBuyer() {
        Transaction transaction = new Transaction();
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionService.create(transaction);
        Transaction found = transactionService.findByBuyer(9L);

        assertNotNull(found);
        assertEquals(9L, found.getBuyer());
    }

    @Test
    void shouldFindBySeller() {
        Transaction transaction = new Transaction();
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionService.create(transaction);
        Transaction found = transactionService.findBySeller(4L);

        assertNotNull(found);
        assertEquals(4L, found.getSeller());
    }

    @Test
    void shouldFindByProduct() {
        Transaction transaction = new Transaction();
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        transactionService.create(transaction);
        Transaction found = transactionService.findByProduct(61L);

        assertNotNull(found);
        assertEquals(61L, found.getProduct());
    }

    @Test
    void shouldDeleteById() {
        Transaction transaction = new Transaction();
        transaction.setSeller(4L);
        transaction.setBuyer(9L);
        transaction.setProduct(61L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(150.0F);

        Transaction saved = transactionService.create(transaction);
        transactionService.deleteById(saved.getId());

        assertThrows(RuntimeException.class, () ->
            transactionService.findById(saved.getId())
        );
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

        transactionService.create(firstT);
        transactionService.create(secondT);

        List<Transaction> founds = transactionService.findAll();

        assertEquals(2, founds.size());
    }
}
