package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.Product;
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
        // ARRANGE
        Market market = new Market();

        // ACT

        Market saved = marketRepository.save(market);

        // ASSERT

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindByID() {
        // ARRANGE
        Market market = new Market();
        marketRepository.save(market);

        // ACT

        Optional<Market> found = marketRepository.findByUserId(market.getId());

        // ASSERT

        assertTrue(found.isPresent());
        assertEquals(market, found.get());
    }

    @Test
    void shouldFindByProduct() {
        // ARRANGE
        Market market = new Market();
        marketRepository.save(market);

        // ACT

        Optional<Market> found = marketRepository.findByProduct(
            market.getProductId()
        );

        // ASSERT

        assertTrue(found.isPresent());
        assertEquals(market, found.get());
    }

    @Test
    void shouldFindByPrice() {
        // ARRANGE
        Market market = new Market();
        marketRepository.save(market);

        // ACT

        Optional<Market> found = marketRepository.findByPrice(
            market.getPrice()
        );

        // ASSERT

        assertTrue(found.isPresent());
        assertEquals(market, found.get());
    }
}
