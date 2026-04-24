package com.api.tinyfarm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.MarketRepository;
import com.api.tinyfarm.repository.TransactionRepository;
import com.api.tinyfarm.repository.UserRepository;
import com.api.tinyfarm.service.MarketService;
import com.api.tinyfarm.service.StockService;
import com.api.tinyfarm.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
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

    @Autowired
    private StockService stockService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MarketRepository marketRepository;

    private static final AtomicLong testCounter = new AtomicLong(0);

    @BeforeEach
    void setup() throws Exception {
        marketRepository.deleteAll();
        stockService.deleteAll();
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(10L);
        market.setUnitPrice(13.0f);
        market.setQuantity(100);
        marketService.create(market);
    }

    @Test
    void shouldCreateMarket() throws Exception {
        Market market = new Market();
        market.setUserId(2L);
        market.setProductId(20L);
        market.setUnitPrice(25.0f);
        market.setQuantity(50);

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
    void shouldReturnMarketByQuantity() throws Exception {
        mockMvc
            .perform(get("/api/market/quantity/100").with(authenticated()))
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
        updatedMarket.setUnitPrice(25.0F);

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

    @Test
    void shouldBuyFromMarket() throws Exception {
        long testId = testCounter.incrementAndGet();

        User seller = new User();
        seller.setName("Seller");
        seller.setEmail("seller_market_" + testId + "@test.com");
        seller.setGender(User.Gender.M);
        seller.setEcus(100.0f);
        seller = userService.create(seller);

        User buyer = new User();
        buyer.setName("Buyer");
        buyer.setEmail("buyer_market_" + testId + "@test.com");
        buyer.setGender(User.Gender.F);
        buyer.setEcus(200.0f);
        buyer = userService.create(buyer);

        Stock sellerStock = new Stock();
        sellerStock.setId(new StockId(seller.getId(), 999L));
        sellerStock.setQuantity(10);
        sellerStock.setCollectible(false);
        stockService.create(sellerStock);

        marketRepository.deleteAll();
        Market listing = new Market();
        listing.setUserId(seller.getId());
        listing.setProductId(999L);
        listing.setUnitPrice(13.0f);
        listing.setQuantity(5);
        marketService.create(listing);

        HashMap<String, Object> request = new HashMap<>();
        request.put("buyerId", buyer.getId());
        request.put("sellerId", seller.getId());
        request.put("productId", 999L);
        request.put("quantity", 2);

        mockMvc
            .perform(
                post("/api/market/buy")
                    .with(authenticated())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());

        User updatedSeller = userService.findById(seller.getId());
        User updatedBuyer = userService.findById(buyer.getId());
        Stock updatedSellerStock = stockService.findById(seller.getId(), 999L);
        Stock updatedBuyerStock = stockService.findById(buyer.getId(), 999L);
        Market updatedListing = marketService.findByUserId(seller.getId());

        assertEquals(126.0f, updatedSeller.getEcus());
        assertEquals(174.0f, updatedBuyer.getEcus());
        assertEquals(8, updatedSellerStock.getQuantity());
        assertEquals(2, updatedBuyerStock.getQuantity());
        assertEquals(5, updatedListing.getQuantity());
    }
}
