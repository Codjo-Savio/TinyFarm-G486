package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
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

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    private Stock testStock;
    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setup() {
        stockService.deleteAll();
        userService.deleteAllUsers();
        productService.deleteAllProducts();

        // Créer et sauvegarder l'User
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser = userService.create(testUser);

        // Créer et sauvegarder le Product
        testProduct = new Product();
        testProduct.setDescription("Test Product");
        testProduct.setPrice(100.0F);
        testProduct = productService.create(testProduct);

        // Créer et sauvegarder le Stock
        testStock = new Stock();
        testStock.setId(new StockId(testUser.getId(), testProduct.getId()));
        testStock.setUser(testUser);
        testStock.setProduct(testProduct);
        testStock.setCollectible(false);
        testStock.setQuantity(1000);
        testStock = stockService.create(testStock);
    }

    // Save / Create Test

    @Test
    void shouldCreateStock() {
        assertNotNull(testStock);
        assertNotNull(testStock.getId());
        assertEquals(testUser.getId(), testStock.getId().getUid());
        assertEquals(testProduct.getId(), testStock.getId().getProductID());
        assertEquals(1000, testStock.getQuantity());
        assertEquals(false, testStock.getCollectible());
    }

    @Test
    void shouldNotCreateStockWithNullId() {
        Stock invalidStock = new Stock();
        invalidStock.setQuantity(500);
        invalidStock.setCollectible(false);

        assertThrows(IllegalArgumentException.class, () ->
            stockService.create(invalidStock)
        );
    }

    @Test
    void shouldNotCreateDuplicateStock() {
        Stock duplicateStock = new Stock();
        duplicateStock.setId(
            new StockId(testUser.getId(), testProduct.getId())
        );
        duplicateStock.setQuantity(500);
        duplicateStock.setCollectible(false);

        assertThrows(IllegalArgumentException.class, () ->
            stockService.create(duplicateStock)
        );
    }

    // Find Test

    @Test
    void shouldFindById() {
        Stock found = stockService.findById(
            testUser.getId(),
            testProduct.getId()
        );

        assertNotNull(found);
        assertEquals(testStock.getId(), found.getId());
        assertEquals(1000, found.getQuantity());
    }

    @Test
    void shouldNotFindByInvalidId() {
        assertThrows(RuntimeException.class, () ->
            stockService.findById(999L, 999L)
        );
    }

    @Test
    void shouldFindByUser() {
        List<Stock> stocks = stockService.findByUser(testUser.getId());

        assertNotNull(stocks);
        assertEquals(1, stocks.size());
        assertEquals(testStock.getId(), stocks.get(0).getId());
    }

    @Test
    void shouldFindByProduct() {
        List<Stock> stocks = stockService.findByProduct(testProduct.getId());

        assertNotNull(stocks);
        assertEquals(1, stocks.size());
        assertEquals(testStock.getId(), stocks.get(0).getId());
    }

    @Test
    void shouldFindAll() {
        // Créer un second stock
        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second@example.com");
        secondUser = userService.create(secondUser);

        Product secondProduct = new Product();
        secondProduct.setDescription("Second Product");
        secondProduct.setPrice(150.0F);
        secondProduct = productService.create(secondProduct);

        Stock secondStock = new Stock();
        secondStock.setId(
            new StockId(secondUser.getId(), secondProduct.getId())
        );
        secondStock.setUser(secondUser);
        secondStock.setProduct(secondProduct);
        secondStock.setQuantity(500);
        secondStock.setCollectible(true);
        stockService.create(secondStock);

        List<Stock> stocks = stockService.findAll();

        assertNotNull(stocks);
        assertEquals(2, stocks.size());
    }

    // Update Test

    @Test
    void shouldUpdateStock() {
        Stock updatedStock = new Stock();
        updatedStock.setQuantity(2000);
        updatedStock.setCollectible(true);

        Stock updated = stockService.update(
            testUser.getId(),
            testProduct.getId(),
            updatedStock
        );

        assertNotNull(updated);
        assertEquals(2000, updated.getQuantity());
        assertEquals(true, updated.getCollectible());
    }

    // Delete Test

    @Test
    void shouldDeleteStockById() {
        stockService.delete(testUser.getId(), testProduct.getId());

        assertThrows(RuntimeException.class, () ->
            stockService.findById(testUser.getId(), testProduct.getId())
        );
    }

    @Test
    void shouldDeleteByUser() {
        Product secondProduct = new Product();
        secondProduct.setDescription("Another Product");
        secondProduct.setPrice(200.0F);
        secondProduct = productService.create(secondProduct);

        Stock secondStock = new Stock();
        secondStock.setId(new StockId(testUser.getId(), secondProduct.getId()));
        secondStock.setUser(testUser);
        secondStock.setProduct(secondProduct);
        secondStock.setQuantity(300);
        stockService.create(secondStock);

        List<Stock> stocksBefore = stockService.findByUser(testUser.getId());
        assertEquals(2, stocksBefore.size());
    }

    @Test
    void shouldDeleteAll() {
        assertEquals(1, stockService.findAll().size());

        stockService.deleteAll();

        assertEquals(0, stockService.findAll().size());
    }
}
