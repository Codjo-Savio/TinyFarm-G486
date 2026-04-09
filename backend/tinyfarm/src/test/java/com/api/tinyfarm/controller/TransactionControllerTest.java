package com.api.tinyfarm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.service.TransactionService;
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
public class TransactionControllerTest
    extends AuthenticatedControllerTestSupport
{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long transactionId;

    @BeforeEach
    void setup() throws Exception {
        transactionService.deleteAll();
        Transaction transaction = new Transaction();
        transaction.setBuyer(10L);
        transaction.setSeller(2L);
        transaction.setProduct(32L);
        transaction.setQuantity(10);
        transaction.setTotalPrice(1000.0F);

        this.transactionId = transactionService.create(transaction).getId();
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setBuyer(13L);
        transaction.setSeller(2L);
        transaction.setProduct(21L);
        transaction.setQuantity(100);
        transaction.setTotalPrice(1240.0F);

        mockMvc
            .perform(
                post("/api/transaction")
                    .with(authenticated())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transaction))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnTransactionById() throws Exception {
        mockMvc
            .perform(
                get("/api/transaction/id/" + transactionId).with(
                    authenticated()
                )
            )
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnTransactionByBuyer() throws Exception {
        mockMvc
            .perform(get("/api/transaction/buyer/10").with(authenticated()))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnTransactionBySeller() throws Exception {
        mockMvc
            .perform(get("/api/transaction/seller/2").with(authenticated()))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnTransactionByProduct() throws Exception {
        mockMvc
            .perform(get("/api/transaction/product/32").with(authenticated()))
            .andExpect(status().isOk());
    }

    @Test
    void transactionShouldNotBeFoundById() throws Exception {
        mockMvc
            .perform(get("/api/transaction/id/999").with(authenticated()))
            .andExpect(status().isNotFound());
    }

    @Test
    void transactionShouldNotBeFoundByBuyer() throws Exception {
        mockMvc
            .perform(get("/api/transaction/buyer/999").with(authenticated()))
            .andExpect(status().isNotFound());
    }

    @Test
    void transactionShouldNotBeFoundBySeller() throws Exception {
        mockMvc
            .perform(get("/api/transaction/seller/999").with(authenticated()))
            .andExpect(status().isNotFound());
    }

    @Test
    void transactionShouldNotBeFoundByProduct() throws Exception {
        mockMvc
            .perform(get("/api/transaction/product/888").with(authenticated()))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTransaction() throws Exception {
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setBuyer(22L);
        updatedTransaction.setSeller(12L);
        updatedTransaction.setProduct(54L);
        updatedTransaction.setQuantity(200);
        updatedTransaction.setTotalPrice(4000.0F);

        mockMvc
            .perform(
                put("/api/transaction/id/" + transactionId)
                    .with(authenticated())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(updatedTransaction)
                    )
            )
            .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteTransactionById() throws Exception {
        mockMvc
            .perform(
                delete("/api/transaction/id/" + transactionId).with(
                    authenticated()
                )
            )
            .andExpect(status().isNoContent());
    }
}
