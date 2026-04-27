package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.MarketID;
import java.util.List;
import java.util.Optional;
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
        market.setUnitPrice(25.0f);
        market.setQuantity(100);

        Market saved = marketRepository.save(market);

        assertNotNull(saved.getMarketId());
        assertEquals(1L, saved.getUserId());
        assertEquals(10L, saved.getProductId());
        assertEquals(25.0f, saved.getUnitPrice());
        assertEquals(100, saved.getQuantity());
    }

    @Test
    void shouldFindByUser() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);
        marketRepository.save(market);

       List<Market> found = marketRepository.findByMarketIdUserId(
            market.getUserId()
       );
        assertEquals(10L, found.getFirst().getProductId());
    }

    @Test
    void shouldFindByProduct() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);
        marketRepository.save(market);

        Optional<Market> found = marketRepository.findByMarketIdProductId(
            market.getProductId()
        );

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getUserId());
    }

    @Test
    void shouldFindByPrice() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);
        marketRepository.save(market);

        List<Market> found = marketRepository.findByPrice(
            market.getUnitPrice()
        );

        assertEquals(10L, found.getFirst().getProductId());
    }

    @Test
    void shouldFindByQuantity() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);
        market.setQuantity(100);
        marketRepository.save(market);

        List<Market> found = marketRepository.findByQuantity(
            market.getQuantity()
        );

        assertEquals(100, found.getFirst().getQuantity());
    }

    @Test
    void shouldDeleteMarketByCompositeKey() {
        Market market = new Market();
        market.setMarketId(new MarketID(1L, 10L));
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(25.0f);
        marketRepository.save(market);

        marketRepository.deleteById(market.getMarketId());

        List<Market> found = marketRepository.findByMarketIdUserId(1L);
        assertEquals(0, found.size());
    }

    @Test
    void shouldFindAllMarkets() {
        Market first = new Market();
        first.setMarketId(new MarketID(1L, 10L));
        first.setUserId(1L);
        first.setProductId(10L);
        first.setUnitPrice(25.0f);

        Market second = new Market();
        second.setMarketId(new MarketID(2L, 20L));
        second.setUserId(2L);
        second.setProductId(20L);
        second.setUnitPrice(13.0f);

        marketRepository.save(first);
        marketRepository.save(second);

        List<Market> markets = marketRepository.findAll();

        assertEquals(2, markets.size());
    }
}
