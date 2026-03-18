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
public class RabbitControllerTest {

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setup() throws Exception {
        mockMvc.perform(delete("/api/rabbits/all"));

        String json = """
                {
                    "userId": 1,
                    "clean": true,
                    "healthy": true,
                    "age": 2,
                    "weight": 3.0,
                    "gender": "M",
                    "name": "Jeannot",
                    "rabbitType": "lapin"
                }
        """;
        mockMvc.perform(
                post("/api/rabbits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
    }

    @Test
    void shouldCreateRabbit() throws Exception {
        String json = """
                {
                    "userId": 1,
                    "clean": true,
                    "healthy": true,
                    "age": 1,
                    "weight": 1.5,
                    "gender": "F",
                    "name": "Marguerite",
                    "rabbitType": "lapereau"
                }
        """;
        mockMvc.perform(
                        post("/api/rabbits")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnAllRabbits() throws Exception {
        mockMvc.perform(get("/api/rabbits"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnRabbitByName() throws Exception {
        mockMvc.perform(get("/api/rabbits/filter/name/Jeannot"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteAllRabbits() throws Exception {
        mockMvc.perform(delete("/api/rabbits/all"))
                .andExpect(status().isNoContent());
    }
}
