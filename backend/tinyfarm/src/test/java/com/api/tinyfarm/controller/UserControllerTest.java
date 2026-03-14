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
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Test
    void shouldCreateUser() throws Exception{
        mockMvc.perform(post("/users"))
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
        mockMvc.perform(get("/user/eldoraldo"))
                .andExpect(status().isOk());
    }

    @Test
    void userShouldNotBeFoundByName() throws  Exception{
        mockMvc.perform(get("/users/bigfarm"))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteUserByName() throws  Exception{
        mockMvc.perform(delete("/user/eldoraldo"))
                .andExpect(status().isNoContent());
    }
}