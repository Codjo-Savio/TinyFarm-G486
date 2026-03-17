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
@AutoConfigureMockMvc(addFilters = false)
public class ChickenControllerTest {
    @Autowired
    MockMvc mockMvc;

    // setup
    @BeforeEach
    void setup() throws  Exception{
        String json = """
                {
                    "id" : 1,
                     "type" : "poule",
                     "name" : "Hermine",
                     "fasting" : "false"
                }
        """;
        mockMvc.perform(
                post("/chickens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
    }

    // tests of the POST
    @Test
    void shouldCreateChicken() throws Exception{
        String json = """
                {
                    "id" : 2,
                     "type" : "poule",
                     "name" : "Clochette",
                     "fasting" : "false"
                }
        """;
        mockMvc.perform(
                        post("/chickens")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
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
        mockMvc.perform(get("/chickens/Hermine"))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteChickenByName() throws  Exception{
        String json = """
                {
                    "id" : 4,
                     "type" : "poule",
                     "name" : "Clémentine",
                     "fasting" : "false"
                }
        """;
        mockMvc.perform(delete("/chickens/Clémentine"))
                .andExpect(status().isNoContent());
    }
}
