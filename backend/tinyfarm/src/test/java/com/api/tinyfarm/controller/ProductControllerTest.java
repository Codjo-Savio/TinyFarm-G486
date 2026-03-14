package com.api.tinyfarm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ProductControllerTest {
    @Autowired
    MockMvc mockMvc;

    // tests of the POST
    @Test
    void shouldCreateProduct() throws Exception{
        mockMvc.perform(post("/products"))
                .andExpect(status().isOk());
    }

    // tests of the GET
    @Test
    void shouldReturnAllProducts() throws  Exception{
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnProductByName() throws  Exception{
        mockMvc.perform(get("/products/foin"))
                .andExpect(status().isOk());
    }

    @Test
    void productShouldNotBeFoundByName() throws  Exception{
         mockMvc.perform(get("/products/viande"))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteProductByName() throws  Exception{
        mockMvc.perform(delete("/products/foin"))
                .andExpect(status().isNoContent());
    }
}
