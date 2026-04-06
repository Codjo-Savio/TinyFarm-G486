package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class StockRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setDescription("Test Product");
        testProduct.setPrice(100.0F);
        testProduct = productRepository.save(testProduct);

        testStock = new Stock();
        testStock.setId(new StockId(testUser.getId(), testProduct.getId()));
        testStock.setUser(testUser);
        testStock.setProduct(testProduct);
        testStock.setCollectible(false);
        testStock.setQuantity(1000);
        testStock = stockRepository.save(testStock);
    }

    // Save Test
    @Test
    void shouldSaveStock() {
        assertNotNull(testStock.getId());
        assertEquals(testUser.getId(), testStock.getUser().getId());
        assertEquals(testProduct.getId(), testStock.getProduct().getId());
        assertEquals(false, testStock.getCollectible());
        assertEquals(1000, testStock.getQuantity());
    }

    // Find Test
    @Test
    void shouldFindById() {
        Optional<Stock> found = stockRepository.findById(
            new StockId(testUser.getId(), testProduct.getId())
        );

        assertTrue(found.isPresent());
        assertEquals(testStock.getId(), found.get().getId());
    }

    @Test
    void shouldFindByUserId() {
        assertNotNull(testStock.getUser());
        assertEquals(testUser.getId(), testStock.getUser().getId());
    }

    @Test
    void shouldFindByProduct() {
        assertNotNull(testStock.getProduct());
        assertEquals(testProduct.getId(), testStock.getProduct().getId());
    }

    @Test
    void shouldFindByCollectible() {
        assertNotNull(testStock.getCollectible());
        assertEquals(false, testStock.getCollectible());
    }

    @Test
    void shouldFindByQuantity() {
        assertNotNull(testStock.getQuantity());
        assertEquals(1000, testStock.getQuantity());
    }

    // Not Find Test
    @Test
    void shouldNotFindById() {
        assertNotNull(testStock.getId());
        assertNotEquals(new StockId(999L, 999L), testStock.getId());
    }

    @Test
    void shouldNotFindByUserId() {
        assertNotNull(testStock.getUser());
        assertNotEquals(999L, testStock.getUser().getId());
    }

    @Test
    void shouldNotFindByProduct() {
        assertNotNull(testStock.getProduct());
        assertNotEquals(999L, testStock.getProduct().getId());
    }

    @Test
    void shouldNotFindByCollectible() {
        assertNotNull(testStock.getCollectible());
        assertNotEquals(true, testStock.getCollectible());
    }

    @Test
    void shouldNotFindByQuantity() {
        assertNotNull(testStock.getQuantity());
        assertNotEquals(20, testStock.getQuantity());
    }

    @Test
    void shouldFindAll() {
        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second@example.com");
        secondUser = userRepository.save(secondUser);

        Product secondProduct = new Product();
        secondProduct.setDescription("Second Product");
        secondProduct.setPrice(150.0F);
        secondProduct = productRepository.save(secondProduct);

        Stock secondStock = new Stock();
        secondStock.setId(
            new StockId(secondUser.getId(), secondProduct.getId())
        );
        secondStock.setUser(secondUser);
        secondStock.setProduct(secondProduct);
        secondStock.setCollectible(true);
        secondStock.setQuantity(1200);
        stockRepository.save(secondStock);

        List<Stock> found = stockRepository.findAll();

        assertEquals(2, found.size());
    }

    // Delete Test
    @Test
    void shouldDeleteStockByCompositeKey() {
        stockRepository.deleteById(testStock.getId());

        Optional<Stock> result = stockRepository.findById(
            new StockId(testUser.getId(), testProduct.getId())
        );
        assertFalse(result.isPresent());
    }
}
