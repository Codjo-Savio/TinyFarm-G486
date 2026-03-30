package com.api.tinyfarm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class ChickenControllerTest extends AuthenticatedControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    // setup
    @BeforeEach
    void setup() throws Exception {
        String json = """
                    {
                        "id" : 1,
                         "chickenType" : "H",
                         "name" : "Hermine",
                         "fastingDays" : 0,
                         "sickDays" : 0
                    }
            """;
        mockMvc.perform(
            post("/api/chickens")
                .with(authenticated())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        );
    }

    // tests of the POST
    @Test
    void shouldCreateChicken() throws Exception {
        String json = """
                    {
                        "id" : 2,
                         "chickenType" : "H",
                         "name" : "Clochette",
                         "fastingDays" : 0,
                          "sickDays" : 0
                    }
            """;
        mockMvc
            .perform(
                post("/api/chickens")
                    .with(authenticated())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isOk());
    }

    // tests of the GET
    @Test
    void shouldReturnAllChickens() throws Exception {
        mockMvc.perform(get("/api/chickens").with(authenticated())).andExpect(status().isOk());
    }

    @Test
    void shouldReturnChickenByName() throws Exception {
        mockMvc
            .perform(get("/api/chickens/name/Hermine").with(authenticated()))
            .andExpect(status().isOk());
    }

    @Test
    void chickenShouldNotBeFoundByName() throws Exception {
        mockMvc
            .perform(get("/api/chickens/name/unknown").with(authenticated()))
            .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteChickenByName() throws Exception {
        mockMvc
            .perform(delete("/api/chickens/name/Clémentine").with(authenticated()))
            .andExpect(status().isNoContent());
    }
}
