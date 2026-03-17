package com.api.tinyfarm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

    // setup
    @BeforeEach
    void setup() throws  Exception{
        String json = """
                {
                    "id" : 1,
                     "description" : "foin",
                     "collection" : "false",
                     "price" : "20"
                }
        """;
        mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
    }

    // tests of the POST
    @Test
    void shouldCreateProduct() throws Exception{
        String json = """
                {
                    "id" : 2,
                     "description" : "paille",
                     "collection" : "false",
                     "price" : "20"
                }
        """;
        mockMvc.perform(post("/api/products"))
                .andExpect(status().isOk());
    }

    // tests of the GET
    @Test
    void shouldReturnAllProducts() throws  Exception{
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnProductById() throws  Exception{
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    void productShouldNotBeFoundById() throws  Exception{
         mockMvc.perform(get("/api/products/3"))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteProductById() throws  Exception{
        String json = """
                {
                    "id" : 4,
                     "description" : "blé",
                     "collection" : "false",
                     "price" : "20"
                }
        """;
        mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
        mockMvc.perform(delete("/api/products/4"))
                .andExpect(status().isNoContent());
    }
}
