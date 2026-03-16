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
        product.setPrice(3.0f);
        product.setCollection(false);

        // ACT
        Product saved = productRepository.save(product);

        // ASSERT
        assertNotNull(saved.getId()); // PK = productID
        assertEquals("Sac de grains", saved.getDescription());
        assertEquals(3.0f, saved.getPrice());
        assertFalse(saved.getCollection());
    }

    @Test
    void shouldFindProductById() {
        // ARRANGE
        Product product = new Product();
        product.setDescription("Seringue");
        product.setPrice(6.0f);
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
    void shouldUpdateProductPrice() {
        // ARRANGE
        Product product = new Product();
        product.setPrice(5.0f);
        productRepository.save(product);

        // ACT
        product.setPrice(10.0f);
        Product updated = productRepository.save(product);

        // ASSERT
        assertEquals(10.0f, updated.getPrice());
    }

    @Test
    void shouldDeleteProduct() {
        // ARRANGE
        Product product = new Product();
        productRepository.save(product);

        // ACT
        productRepository.deleteById(product.getId());

        // ASSERT
        Optional<Product> found = productRepository.findById(product.getId());
        assertFalse(found.isPresent());
    }
}