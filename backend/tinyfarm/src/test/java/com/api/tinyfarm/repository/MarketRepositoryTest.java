package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.MarketID;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class MarketRepositoryTest {

    @Autowired
    private MarketRepository marketRepository;

    @BeforeEach
    void setUp() {
        marketRepository.deleteAll();
    }

    @Test
    void shouldSaveMarket() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setPrice(25.0f);

        Market saved = marketRepository.save(market);

        assertNotNull(saved.getMarketId());
        assertEquals(1L, saved.getUserId());
        assertEquals(10L, saved.getProductId());
        assertEquals(25.0f, saved.getPrice());
    }

    @Test
    void shouldFindByUser() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setPrice(25.0f);
        marketRepository.save(market);

        Optional<Market> found = marketRepository.findByUserId(market.getUserId());

        assertTrue(found.isPresent());
        assertEquals(10L, found.get().getProductId());
    }

    @Test
    void shouldFindByProduct() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setPrice(25.0f);
        marketRepository.save(market);

        Optional<Market> found = marketRepository.findByProductId(market.getProductId());

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getUserId());
    }

    @Test
    void shouldFindByPrice() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setPrice(25.0f);
        marketRepository.save(market);

        Optional<Market> found = marketRepository.findByPrice(market.getPrice());

        assertTrue(found.isPresent());
        assertEquals(10L, found.get().getProductId());
    }

    @Test
    void shouldDeleteMarketByCompositeKey() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setPrice(25.0f);
        marketRepository.save(market);

        marketRepository.deleteById(market.getMarketId());

        Optional<Market> found = marketRepository.findByUserId(1L);
        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindAllMarkets() {
        Market first = new Market();
        first.setMarketId(new MarketID(1L, 10L));
        first.setUserId(1L);
        first.setProductId(10L);
        first.setPrice(25.0f);

        Market second = new Market();
        second.setMarketId(new MarketID(2L, 20L));
        second.setUserId(2L);
        second.setProductId(20L);
        second.setPrice(13.0f);

        marketRepository.save(first);
        marketRepository.save(second);

        List<Market> markets = marketRepository.findAll();

        assertEquals(2, markets.size());
    }
}
