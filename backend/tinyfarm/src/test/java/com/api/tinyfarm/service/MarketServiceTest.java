package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void shouldCreateMarket() {
        Market market = new Market();

        User user = new User();
        market.setUserId(user.getId());

        Product product = new Product();
        product.setDescription("blé");
        product.setPrice(25.0f);

        market.setProductId(product.getId());

        Market created = marketService.create(market);

        assertNotNull(marketService.findById(user.getId()));
    }

    @Test
    void shouldReturnAllProducts() {
        Market market = new Market();
        Market modifiedMarket = new Market();

        Product product = new Product();
        product.setDescription("blé");
        product.setPrice(25.0f);

        modifiedMarket.setProductId(product.getId());
        modifiedMarket.setPrice(product.getPrice());
        marketService.update(modifiedMarket.getUserId(), modifiedMarket);

        Product anotherProduct = new Product();
        product.setDescription("foin");
        product.setPrice(25.0f);

        modifiedMarket.setProductId(anotherProduct.getId());
        modifiedMarket.setPrice(anotherProduct.getPrice());
        marketService.update(modifiedMarket.getUserId(), modifiedMarket);

        modifiedMarket.setUserId(market.getUserId());
        marketService.update(market.getUserId(), modifiedMarket);

        assertNotNull(marketService.findByProductId(product.getId()));
        assertNotNull(marketService.findByProductId(anotherProduct.getId()));
    }

    @Test
    void shouldDeleteProduct() {

        User user = new User();
        Market market = new Market();
        Product product = new Product();

        market.setUserId(user.getId());
        market.setProductId(product.getId());

        marketService.deleteProductById(user.getId(), product.getId());

        assertNull(market.getProductId());
    }
}
