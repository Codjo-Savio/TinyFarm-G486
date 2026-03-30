package com.api.tinyfarm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.tinyfarm.model.Market;
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
public class MarketControllerTest extends AuthenticatedControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MarketService marketService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        marketService.deleteAll();
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(10L);
        market.setPrice(13.0f);
        marketService.create(market);
    }

    @Test
    void shouldCreateMarket() throws Exception {
        Market market = new Market();
        market.setUserId(2L);
        market.setProductId(20L);
        market.setPrice(25.0f);

        mockMvc
            .perform(
                post("/api/market")
                    .with(authenticated())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(market))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnMarketByUserId() throws Exception {
        mockMvc
            .perform(get("/api/market/id/1").with(authenticated()))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnMarketByProductId() throws Exception {
        mockMvc
            .perform(get("/api/market/product/10").with(authenticated()))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnMarketByPrice() throws Exception {
        mockMvc
            .perform(get("/api/market/price/13.0").with(authenticated()))
            .andExpect(status().isOk());
    }

    @Test
    void marketShouldNotBeFoundByUserId() throws Exception {
        mockMvc
            .perform(get("/api/market/id/999").with(authenticated()))
            .andExpect(status().isNotFound());
    }

    @Test
    void marketShouldNotBeFoundByProductId() throws Exception {
        mockMvc
            .perform(get("/api/market/product/999").with(authenticated()))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateMarket() throws Exception {
        Market updatedMarket = new Market();
        updatedMarket.setUserId(1L);
        updatedMarket.setProductId(10L);
        updatedMarket.setPrice(25.0F);

        mockMvc
            .perform(
                put("/api/market/id/1")
                    .with(authenticated())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedMarket))
            )
            .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteMarketByUserIdAndProductId() throws Exception {
        mockMvc
            .perform(delete("/api/market/1/10").with(authenticated()))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteMarketByUserId() throws Exception {
        mockMvc
            .perform(delete("/api/market/id/1").with(authenticated()))
            .andExpect(status().isNoContent());
    }
}
