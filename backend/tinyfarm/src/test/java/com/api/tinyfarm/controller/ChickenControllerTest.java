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
public class ChickenControllerTest {
    @Autowired
    MockMvc mockMvc;

    // tests of the POST
    @Test
    void shouldCreateChicken() throws Exception{
        mockMvc.perform(post("/chickens"))
                .andExpect(status().isOk());
    }

    // tests of the GET
    @Test
    void shouldReturnAllChickens() throws  Exception{
        mockMvc.perform(get("/chickens"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnChickenByName() throws  Exception{
        mockMvc.perform(get("/chickens/Hermine"))
                .andExpect(status().isOk());
    }

    @Test
    void chickenShouldNotBeFoundByName() throws  Exception{
        mockMvc.perform(get("/chickens/Clochette"))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteChickenByName() throws  Exception{
        mockMvc.perform(delete("/chickens/Hermine"))
                .andExpect(status().isNoContent());
    }
}
