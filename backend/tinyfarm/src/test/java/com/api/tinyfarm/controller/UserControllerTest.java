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
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    // setup
    @BeforeEach
    void setup() throws  Exception{
        String json = """
                {
                     "id" : 1,
                     "nom" : "Eldoraldo",
                     "sexe" : "F",
                     "ecus" : "10",
                     "level" : "1"
                }
        """;
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
        );
    }

    // tests of the POST  
    @Test
    void shouldCreateUser() throws Exception{
        String json = """
                {
                     "nom" : "Bigfarm",
                     "sexe" : "F",
                     "ecus" : "10",
                     "level" : "1"
                }
        """;
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }

    // tests of the GET
    @Test
    void shouldReturnUsers() throws Exception{
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUserByName() throws  Exception{
        mockMvc.perform(get("/users/Eldoraldo"))
                .andExpect(status().isOk());
    }

    @Test
    void userShouldNotBeFoundByName() throws  Exception{
        mockMvc.perform(get("/users/unknown"))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteUserByName() throws  Exception{
        mockMvc.perform(delete("/users/Eldoraldo"))
                .andExpect(status().isNoContent());
    }
}