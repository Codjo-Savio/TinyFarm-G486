package com.api.tinyfarm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ProductControllerTest extends AuthenticatedControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    private Long productId;

    // setup
    @BeforeEach
    void setup() throws Exception {
        String json = """
                {
                     "description" : "foin",
                     "collectible" : false,
                     "coefficient" : 1
                }
        """;

        String response = mockMvc.perform(
                post("/api/products")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andReturn().getResponse().getContentAsString();

        productId = new ObjectMapper()
                .readTree(response)
                .get("id")
                .asLong();
    }

    // tests of the POST
    @Test
    void shouldCreateProduct() throws Exception {
        String json = """
                {
                     "description" : "paille",
                     "collectible" : false,
                     "coefficient" : 1
                }
        """;

        mockMvc.perform(post("/api/products")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // tests of the GET
    @Test
    void shouldReturnAllProducts() throws Exception {
        mockMvc.perform(get("/api/products").with(authenticated()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnProductById() throws Exception {
        mockMvc.perform(get("/api/products/id/" + productId).with(authenticated()))
                .andExpect(status().isOk());
    }

    @Test
    void productShouldNotBeFoundById() throws Exception {
        mockMvc.perform(get("/api/products/id/999").with(authenticated()))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteProductById() throws Exception {
        mockMvc.perform(delete("/api/products/id/" + productId).with(authenticated()))
                .andExpect(status().isNoContent());
    }
}