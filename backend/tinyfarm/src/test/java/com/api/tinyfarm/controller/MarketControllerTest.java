package com.api.tinyfarm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.MarketID;
import com.api.tinyfarm.service.MarketService;
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
public class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MarketService marketService;

    @Autowired
    private ObjectMapper objectMapper;

    private Market testMarket;

    @BeforeEach
    void setup() throws Exception {
        marketService.deleteAll();

        // Créer une annonce de test
        testMarket = new Market();
        testMarket.setMarketId(new MarketID(1L, 62L));
        testMarket.setUserId(1L); // L pour Long
        testMarket.setProductId(62L);
        testMarket.setPrice(13.0F); // F pour Float

        mockMvc
            .perform(
                post("/api/market")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testMarket))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateMarket() throws Exception {
        Market newMarket = new Market();
        newMarket.setMarketId(new MarketID(2L, 53L));
        newMarket.setUserId(2L);
        newMarket.setProductId(53L);
        newMarket.setPrice(32.0F);

        mockMvc
            .perform(
                post("/api/market")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newMarket))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnMarketByUserId() throws Exception {
        mockMvc.perform(get("/api/market/id/1")).andExpect(status().isOk());
    }

    @Test
    void shouldReturnMarketByProductId() throws Exception {
        mockMvc
            .perform(get("/api/market/productId/62"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnMarketByPrice() throws Exception {
        mockMvc
            .perform(get("/api/market/price/13.0"))
            .andExpect(status().isOk());
    }

    @Test
    void marketShouldNotBeFoundByUserId() throws Exception {
        mockMvc
            .perform(get("/api/market/id/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void marketShouldNotBeFoundByProductId() throws Exception {
        mockMvc
            .perform(get("/api/market/productId/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateMarket() throws Exception {
        Market updatedMarket = new Market();
        updatedMarket.setMarketId(new MarketID(1L, 62L));
        updatedMarket.setUserId(1L);
        updatedMarket.setProductId(62L);
        updatedMarket.setPrice(25.0F);

        mockMvc
            .perform(
                put("/api/market/id/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedMarket))
            )
            .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteMarketByUserIdAndProductId() throws Exception {
        mockMvc
            .perform(delete("/api/market/1/62"))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteMarketByUserId() throws Exception {
        mockMvc
            .perform(delete("/api/market/id/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteAllMarkets() throws Exception {
        mockMvc
            .perform(delete("/api/market"))
            .andExpect(status().isNoContent());
    }
}
