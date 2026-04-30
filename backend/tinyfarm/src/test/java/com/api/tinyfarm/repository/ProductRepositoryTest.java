package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Product;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldSaveProduct() {
        // ARRANGE
        Product product = new Product();
        product.setDescription("Sac de grains");
        product.setCollectible(false);

        // ACT
        Product saved = productRepository.save(product);

        // ASSERT
        assertNotNull(saved.getId()); // PK = productID
        assertEquals("Sac de grains", saved.getDescription());
        assertFalse(saved.getCollectible());
    }

    @Test
    void shouldFindProductById() {
        // ARRANGE
        Product product = new Product();
        product.setDescription("Seringue");
        productRepository.save(product);

        // ACT
        Optional<Product> found = productRepository.findById(product.getId());

        // ASSERT
        assertTrue(found.isPresent());
        assertEquals("Seringue", found.get().getDescription());
    }

    @Test
    void shouldFindAllProducts() {
        // ARRANGE
        Product p1 = new Product();
        p1.setDescription("Savon");

        Product p2 = new Product();
        p2.setDescription("Seau d'eau");

        productRepository.save(p1);
        productRepository.save(p2);

        // ACT
        List<Product> products = productRepository.findAll();

        // ASSERT
        assertEquals(2, products.size());
    }

    @Test
    void shouldUpdateProductDescription() {
        // ARRANGE
        Product product = new Product();
        product.setDescription("Foin");
        productRepository.save(product);

        // ACT
        product.setDescription("Foin premium");
        Product updated = productRepository.save(product);

        // ASSERT
        assertEquals("Foin premium", updated.getDescription());
    }

    @Test
    void shouldDeleteProduct() {
        // ARRANGE
        Product product = new Product();
        product.setDescription("Foin");
        productRepository.save(product);

        // ACT
        productRepository.deleteById(product.getId());

        // ASSERT
        Optional<Product> found = productRepository.findById(product.getId());
        assertFalse(found.isPresent());
    }
}
