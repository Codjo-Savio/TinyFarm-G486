package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        // On instancie les variables nécéssaires pour market :
        // Et on les attribut au market.
        Market market = new Market();

        User user = new User();
        market.setUserId(user.getId());

        Product product = new Product();
        product.setDescription("blé");
        product.setPrice(25.0f);

        market.setProductId(product.getId());

        Market created = marketService.create(market);

        // Assert : On vérifie que le market à bien été crée.

        assertNotNull(marketService.findById(user.getId()));
    }

    @Test
    void shouldReturnAllProducts() {
        Market market = new Market();
        Market modifiedMarket = new Market();

        Product product = new Product();
        product.setDescription("blé");
        product.setPrice(25.0f);

        // On ajoute le nouveau produit dans le modified market

        modifiedMarket.setProductId(product.getId());
        modifiedMarket.setPrice(product.getPrice());
        marketService.update(modifiedMarket.getUserId(), modifiedMarket);

        // On fait la même chose pour le deuxieme

        Product anotherProduct = new Product();
        product.setDescription("foin");
        product.setPrice(25.0f);

        modifiedMarket.setProductId(anotherProduct.getId());
        modifiedMarket.setPrice(anotherProduct.getPrice());
        marketService.update(modifiedMarket.getUserId(), modifiedMarket);

        // On transfère les nouvelles informations dans le nouveau market.

        modifiedMarket.setUserId(market.getUserId());
        marketService.update(market.getUserId(), modifiedMarket);

        // ASSERT
        // On vérifie que les deux nouveaux produits sont dans le market.
        assertNotNull(marketService.findByProductId(product.getId()));
        assertNotNull(marketService.findByProductId(anotherProduct.getId()));
    }

    @Test
    void shouldDeleteProduct(Long userId, Long productId) {
        // On instancie un market.
        Market market = new Market();

        market.setUserId(userId);
        market.setProductId(productId);

        // On supprime le produit dans le market.
        marketService.deleteProductById(userId, productId);

        // Assert ( On vérifie que le produit est bien supprimé. )
        // c.a.d que le productId du market est null.

        assertNull(market.getProductId());
    }
}
