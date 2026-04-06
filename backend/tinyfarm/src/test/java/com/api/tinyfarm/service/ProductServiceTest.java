package com.api.tinyfarm.service;


import com.api.tinyfarm.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class ProductServiceTest {
    @Autowired
    ProductService productService;

    @BeforeEach
    void setUp(){
        productService.deleteAllProducts();
    }

    @Test
    void shouldCreateProduct() {
        Product product = new Product();
        product.setDescription("blé");
        product.setPrice(25.0f);

        Product created = productService.add(product);

        assertNotNull(created.getId());
        assertEquals(false, created.getCollectible());
        assertEquals(1, created.getCoefficient());
    }

    @Test
    void shouldReturnAllProducts() {
        Product product = new Product();
        product.setDescription("blé");
        product.setPrice(25.0f);

        Product created = productService.add(product);

        Product anotherProduct = new Product();
        anotherProduct.setDescription("foin");
        anotherProduct.setPrice(25.0f);

        Product anotherProductCreated = productService.add(anotherProduct);

        assertNotNull(productService.findAll());
    }

    @Test
    void shouldDeleteProduct(){
        Product product = new Product();
        product.setDescription("blé");
        product.setPrice(25.0f);

        Product created = productService.add(product);
        productService.delete(created.getId());

        assertEquals(0, productService.findAll().size());
    }

}
