package com.api.tinyfarm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StockControllerTest extends AuthenticatedControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockService stockService;

    @Autowired
    private ObjectMapper objectMapper;

    private Stock testStock;
    private Long testUserId = 1L;
    private Long testProductId = 10L;

    @BeforeEach
    void setup() throws Exception {
        stockService.deleteAll();

        // Créer et sauvegarder le Stock avec des IDs simples
        testStock = new Stock();
        testStock.setId(new StockId(testUserId, testProductId));
        testStock.setCollectible(false);
        testStock.setQuantity(1000);
        testStock = stockService.create(testStock);
    }

    // Create Test
    @Test
    void shouldCreateStock() throws Exception {
        Stock newStock = new Stock();
        newStock.setId(new StockId(2L, 20L));
        newStock.setCollectible(false);
        newStock.setQuantity(500);

        mockMvc
            .perform(
                post("/api/stocks")
                    .with(authenticated())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newStock))
            )
            .andExpect(status().isCreated());
    }

    // Find Test
    @Test
    void shouldFindById() throws Exception {
        mockMvc
            .perform(
                get(
                    "/api/stocks/user/" +
                        testUserId +
                        "/product/" +
                        testProductId
                ).with(authenticated())
            )
            .andExpect(status().isOk());
    }

    @Test
    void shouldFindByUser() throws Exception {
        mockMvc
            .perform(
                get("/api/stocks/user/" + testUserId).with(authenticated())
            )
            .andExpect(status().isOk());
    }

    @Test
    void shouldFindByProduct() throws Exception {
        mockMvc
            .perform(
                get("/api/stocks/product/" + testProductId).with(
                    authenticated()
                )
            )
            .andExpect(status().isOk());
    }

    @Test
    void shouldFindAll() throws Exception {
        mockMvc
            .perform(get("/api/stocks").with(authenticated()))
            .andExpect(status().isOk());
    }

    // Not Find Test
    @Test
    void shouldNotFindById() throws Exception {
        mockMvc
            .perform(
                get("/api/stocks/user/999/product/999").with(authenticated())
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotFindByUser() throws Exception {
        mockMvc
            .perform(get("/api/stocks/user/999").with(authenticated()))
            .andExpect(status().isOk());
    }

    @Test
    void shouldNotFindByProduct() throws Exception {
        mockMvc
            .perform(get("/api/stocks/product/999").with(authenticated()))
            .andExpect(status().isOk());
    }

    // Update Test
    @Test
    void shouldUpdateStock() throws Exception {
        Stock updatedStock = new Stock();
        updatedStock.setQuantity(2000);
        updatedStock.setCollectible(true);

        mockMvc
            .perform(
                put(
                    "/api/stocks/user/" +
                        testUserId +
                        "/product/" +
                        testProductId
                )
                    .with(authenticated())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedStock))
            )
            .andExpect(status().isOk());
    }

    // Delete Test
    @Test
    void shouldDeleteStockById() throws Exception {
        mockMvc
            .perform(
                delete(
                    "/api/stocks/user/" +
                        testUserId +
                        "/product/" +
                        testProductId
                ).with(authenticated())
            )
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteStockByUser() throws Exception {
        Long userToDeleteId = 3L;
        Long productForDeleteId = 30L;

        Stock stockForDelete = new Stock();
        stockForDelete.setId(new StockId(userToDeleteId, productForDeleteId));
        stockForDelete.setQuantity(100);
        stockForDelete.setCollectible(false);
        stockService.create(stockForDelete);

        mockMvc
            .perform(
                delete("/api/stocks/user/" + userToDeleteId).with(
                    authenticated()
                )
            )
            .andExpect(status().isNoContent());
    }
}
