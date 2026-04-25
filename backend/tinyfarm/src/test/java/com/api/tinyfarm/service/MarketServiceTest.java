package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.repository.MarketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class MarketServiceTest {

    @Autowired
    private MarketService marketService;
    @Autowired
    private MarketRepository marketRepository;

    @BeforeEach
    void setup() {
        marketRepository.deleteAll();
    }

    @Test
    void shouldCreateMarket() {
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);
        market.setQuantity(100);

        Market created = marketService.create(market);

        assertNotNull(created.getMarketId());
        assertEquals(1L, created.getUserId());
        assertEquals(10L, created.getProductId());
        assertEquals(100, created.getQuantity());
    }

    @Test
    void shouldReturnMarketByProductId() {
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);

        marketService.create(market);

        Market found = marketService.findByProductId(10L);

        assertNotNull(found);
        assertEquals(1L, found.getUserId());
    }

    @Test
    void shouldDeleteMarketByUserIdAndProductId() {
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);
        marketService.create(market);

        marketService.deleteProductById(1L, 10L);

        assertEquals(0, marketService.findAll().size());
    }

    @Test
    void shouldDeleteMarketByUserId() {
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);
        marketService.create(market);

        marketService.deleteByID(1L);

        assertEquals(0, marketService.findAll().size());
    }
}
