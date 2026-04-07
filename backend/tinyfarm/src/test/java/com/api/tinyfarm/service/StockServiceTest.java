package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class StockServiceTest {

    @Autowired
    private StockService stockService;

    private Stock testStock;
    private Long testUserId;
    private Long testProductId;

    private UserService userService;

    @BeforeEach
    void setup() throws Exception {
        stockService.deleteAll();

        testUserId = 1L;
        testProductId = 10L;

        testStock = new Stock();
        testStock.setId(new StockId(testUserId, testProductId));
        testStock.setQuantity(1000);
        testStock.setCollectible(false);
        testStock = stockService.create(testStock);
    }

    @Test
    void shouldCreateStock() {
        assertNotNull(testStock);
        assertNotNull(testStock.getId());
        assertEquals(testUserId, testStock.getId().getUid());
        assertEquals(testProductId, testStock.getId().getProductID());
        assertEquals(1000, testStock.getQuantity());
        assertEquals(false, testStock.getCollectible());
    }

    @Test
    void shouldFindById() {
        Stock found = stockService.findById(testUserId, testProductId);

        assertNotNull(found);
        assertEquals(testStock.getId(), found.getId());
        assertEquals(1000, found.getQuantity());
    }

    @Test
    void shouldFindByUser() {
        List<Stock> stocks = stockService.findByUser(testUserId);

        assertNotNull(stocks);
        assertEquals(1, stocks.size());
        assertEquals(testStock.getId(), stocks.get(0).getId());
    }

    @Test
    void shouldFindByProduct() {
        List<Stock> stocks = stockService.findByProduct(testProductId);

        assertNotNull(stocks);
        assertEquals(1, stocks.size());
        assertEquals(testStock.getId(), stocks.get(0).getId());
    }

    @Test
    void shouldFindAll() throws Exception {
        Long secondUserId = 2L;
        Long secondProductId = 20L;

        Stock secondStock = new Stock();
        secondStock.setId(new StockId(secondUserId, secondProductId));
        secondStock.setQuantity(500);
        secondStock.setCollectible(true);
        stockService.create(secondStock);

        List<Stock> stocks = stockService.findAll();

        assertNotNull(stocks);
        assertEquals(2, stocks.size());
    }

    @Test
    void shouldUpdateStock() {
        Stock updatedStock = new Stock();
        updatedStock.setQuantity(2000);
        updatedStock.setCollectible(true);

        Stock updated = stockService.update(
            testUserId,
            testProductId,
            updatedStock
        );

        assertNotNull(updated);
        assertEquals(2000, updated.getQuantity());
        assertEquals(true, updated.getCollectible());
    }

    @Test
    void shouldDeleteStockById() {
        stockService.delete(testUserId, testProductId);

        assertThrows(RuntimeException.class, () ->
            stockService.findById(testUserId, testProductId)
        );
    }

    @Test
    void shouldDeleteByUser() throws Exception {
        Long secondProductId = 20L;

        Stock secondStock = new Stock();
        secondStock.setId(new StockId(testUserId, secondProductId));
        secondStock.setQuantity(300);
        secondStock.setCollectible(false);
        stockService.create(secondStock);

        List<Stock> stocksBefore = stockService.findByUser(testUserId);
        assertEquals(2, stocksBefore.size());

        stockService.deleteByUser(testUserId);

        List<Stock> stocksAfter = stockService.findByUser(testUserId);
        assertEquals(0, stocksAfter.size());
    }

    @Test
    void shouldDeleteAll() {
        assertEquals(1, stockService.findAll().size());

        stockService.deleteAll();

        assertEquals(0, stockService.findAll().size());
    }

    @Test
    void shouldModifyStockByBuying() {
        /* Stock
        testUserId = 1L;
        testProductId = 10L;

        testStock = new Stock();
        testStock.setId(new StockId(testUserId, testProductId));
        testStock.setQuantity(1000);
        testStock.setCollectible(false);
        testStock = stockService.create(testStock);
        */

        // Création d'une transaction test

        Transaction transaction = new Transaction();
        transaction.setId(13L);
        transaction.setBuyer(testUserId);
        transaction.setSeller(24L);
        transaction.setProduct(testProductId);
        transaction.setQuantity(250);
        transaction.setTotalPrice(500);

        // Création d'un User
        User buyer = new User();
        buyer.setId(2L);
        buyer.setEmail("buyer@lovetobuy.com");
        buyer.setGender(User.Gender.F);
        buyer.setHibernation(false);
        buyer.setLevel(10);
        buyer.setName("Véronica");
        buyer.setEcus(1500F);

        stockService.buy(transaction.getId());
        Stock sfound = stockService.findById(testUserId, testProductId);
        User uFound = userService.find

        // Test Quantité de produit
        assertEquals(testStock.getProductId(), found.getProductId());
        assertEquals(
            testStock.getQuantity() - transaction.getQuantity(),
            found.getQuantity()
        );
        // Test Ecus
        assertEquals(buyer.getEcus() - transaction.getTotalPrice(), buyer);
    }

    @Test
    void shouldModifyStockBySelling() {}
}
