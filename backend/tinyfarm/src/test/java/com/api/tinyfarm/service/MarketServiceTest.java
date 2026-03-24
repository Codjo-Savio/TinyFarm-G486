package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class MarketServiceTest {

    @Autowired
    MarketService marketService;

    @Autowired
    ProductService productService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setup() {
        marketService.deleteAll();
        productService.deleteAllProducts();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnAllProducts() {
        Market Market = new Market();
        Market modifiedMarket = new Market();

        Product product = new Product();
        product.setDescription("blé");
        product.setPrice(25.0f);

        modifiedMarket.setProductId(product.getId());

        marketService.update(modifiedMarket.getUserId(), modifiedMarket);

        Product created = productService.add(product);

        Product anotherProduct = new Product();
        product.setDescription("foin");
        product.setPrice(25.0f);

        Product anotherProductCreated = productService.add(anotherProduct);

        assertNotNull(productService.findAll());
    }
}
