package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class StockRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAll();
    }

    @Test
    void shouldSaveStock() {
        Stock stock = new Stock();
        stock.setId(new StockId(1L, 10L));
        stock.setUserId(1L);
        stock.setProductId(10L);
        stock.setQuantity(1000);
        stock.setCollectible(false);

        Stock saved = stockRepository.save(stock);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getUserId());
        assertEquals(10L, saved.getProductId());
        assertEquals(1000, saved.getQuantity());
        assertEquals(false, saved.getCollectible());
    }

    @Test
    void shouldFindById() {
        Stock stock = new Stock();
        stock.setId(new StockId(1L, 10L));
        stock.setUserId(1L);
        stock.setProductId(10L);
        stock.setQuantity(500);
        stock.setCollectible(true);
        stockRepository.save(stock);

        Optional<Stock> found = stockRepository.findById(stock.getId());

        assertTrue(found.isPresent());
        assertEquals(new StockId(1L, 10L), found.get().getId());
        assertEquals(500, found.get().getQuantity());
        assertEquals(true, found.get().getCollectible());
    }

    @Test
    void shouldDeleteStockByCompositeKey() {
        Stock stock = new Stock();
        stock.setId(new StockId(1L, 10L));
        stock.setUserId(1L);
        stock.setProductId(10L);
        stock.setQuantity(200);
        stock.setCollectible(false);
        stockRepository.save(stock);

        stockRepository.deleteById(stock.getId());

        Optional<Stock> result = stockRepository.findById(new StockId(1L, 10L));
        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindAllStocks() {
        Stock first = new Stock();
        first.setId(new StockId(1L, 10L));
        first.setUserId(1L);
        first.setProductId(10L);
        first.setQuantity(1000);
        first.setCollectible(false);

        Stock second = new Stock();
        second.setId(new StockId(2L, 20L));
        second.setUserId(2L);
        second.setProductId(20L);
        second.setQuantity(500);
        second.setCollectible(true);

        stockRepository.save(first);
        stockRepository.save(second);

        List<Stock> stocks = stockRepository.findAll();

        assertEquals(2, stocks.size());
    }
}
