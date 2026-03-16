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
                     "name" : "Eldoraldo",
                     "gender" : "F",
                     "ecus" : "10",
                     "level" : "1"
                }
        """;
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
        );
    }

    // tests of the POST  
    @Test
    void shouldCreateUser() throws Exception{
        String json = """
                {
                    "id" : 2,
                     "name" : "Bigfarm",
                     "gender" : "M",
                     "ecus" : "10",
                     "level" : "1"
                }
        """;
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }

    // tests of the GET
    @Test
    void shouldReturnUsers() throws Exception{
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUserById() throws  Exception{
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void userShouldNotBeFoundById() throws  Exception{
        mockMvc.perform(get("/api/users/0"))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteUserById() throws  Exception{
        String json = """
                {
                    "id" : 3,
                     "name" : "Colorado",
                     "gender" : "F",
                     "ecus" : "100",
                     "level" : "1"
                }
        """;
        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
        mockMvc.perform(delete("/api/users/3"))
                .andExpect(status().isNoContent());
    }
}