package com.api.tinyfarm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.StockRepository;
import com.api.tinyfarm.repository.UserRepository;
import com.api.tinyfarm.service.StockService;
import com.api.tinyfarm.service.TransactionService;
import com.api.tinyfarm.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class StockControllerTest extends AuthenticatedControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockService stockService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long testUserId = 1L;
    private final Long testProductId = 10L;

    private Transaction transaction;

    private static final AtomicLong testCounter = new AtomicLong(0);

    @BeforeEach
    void setup() throws Exception {
        long testId = testCounter.incrementAndGet();

        // Clean up all data for test isolation
        stockService.deleteAll();
        transactionService.deleteAll();
        userRepository.deleteAll();

        // Create test stock
        Stock testStock = new Stock();
        testStock.setId(new StockId(testUserId, testProductId));
        testStock.setCollectible(false);
        testStock.setQuantity(1000);
        testStock = stockService.create(testStock);

        // Create seller with unique email
        User seller = new User();
        seller.setName("Vendeur Test");
        seller.setEmail("vendeur_" + testId + "@test.com");
        seller.setEcus(100.0f);
        seller.setGender(User.Gender.F);
        seller.setHibernation(false);
        seller = userService.create(seller);

        // Create buyer with unique email
        User buyer = new User();
        buyer.setName("Acheteur Test");
        buyer.setEmail("acheteur_" + testId + "@test.com");
        buyer.setEcus(200.0f);
        buyer.setGender(User.Gender.M);
        buyer.setHibernation(false);
        buyer = userService.create(buyer);

        // Create seller's stock
        Stock sellerStock = new Stock();
        sellerStock.setId(new StockId(seller.getId(), 1L));
        sellerStock.setQuantity(10);
        sellerStock.setCollectible(false);
        sellerStock = stockService.create(sellerStock);

        // Create transaction
        transaction = new Transaction();
        transaction.setSeller(seller.getId());
        transaction.setBuyer(buyer.getId());
        transaction.setProduct(1L);
        transaction.setQuantity(3);
        transaction.setTotalPrice(30.0f);
        transaction = transactionService.create(transaction);
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

    // Find Tests
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

    // Not Found Tests
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

    // Delete Tests
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

    // Legacy route removed from StockController
    @Test
    void shouldReturnNotFoundForLegacySellRoute() throws Exception {
        mockMvc
            .perform(
                post("/api/stocks/sell/{tid}", transaction.getId()).with(
                    authenticated()
                )
            )
            .andExpect(status().isNotFound());
    }

    // Legacy route removed from StockController
    @Test
    void shouldReturnNotFoundForLegacyBuyRoute() throws Exception {
        mockMvc
            .perform(
                post("/api/stocks/buy/{tid}", transaction.getId()).with(
                    authenticated()
                )
            )
            .andExpect(status().isNotFound());
    }
}
