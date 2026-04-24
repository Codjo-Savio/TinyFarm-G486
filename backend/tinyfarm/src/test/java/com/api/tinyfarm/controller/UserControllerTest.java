package com.api.tinyfarm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.api.tinyfarm.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerTest extends AuthenticatedControllerTestSupport {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private Long createdUserId;

    // setup
    @BeforeEach
    void setup() {
        userService.deleteAllUsers();
        var user = new com.api.tinyfarm.model.User();
        user.setName("Eldoraldo");
        user.setEmail("usertest@gmail.com");
        user.setGender(com.api.tinyfarm.model.User.Gender.F);
        user.setEcus(10F);
        user.setLevel(1);
        createdUserId = userService.create(user).getId();
    }

    // tests of the POST
    @Test
    void shouldCreateUser() throws Exception {
        String json = """
                        {
                            "id" : 2,
                             "name" : "Bigfarm",
                             "email" : "usertest@gmail.com",
                             "gender" : "M",
                             "ecus" : "10",
                             "level" : "1"
                        }
                """;
        mockMvc.perform(
                post("/api/users")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUserById() throws Exception {
        mockMvc.perform(get("/api/users/id/" + createdUserId).with(authenticated()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnRemainingPurchasesByUserId() throws Exception {
        mockMvc.perform(get("/api/users/remainingPurchases/id/" + createdUserId).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(12));
    }

    @Test
    void userShouldNotBeFoundById() throws Exception {
        mockMvc.perform(get("/api/users/id/0").with(authenticated()))
                .andExpect(status().isNotFound());
    }

    // test of the DELETE
    @Test
    void shouldDeleteUserById() throws Exception {
        String json = """
                        {
                            "id" : 3,
                             "name" : "Colorado",
                             "email" : "usertest@gmail.com",
                             "gender" : "F",
                             "ecus" : "100",
                             "level" : "1"
                        }
                """;
        mockMvc.perform(
                post("/api/users")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));
        mockMvc.perform(delete("/api/users/id/3").with(authenticated()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldHibernateUser() throws Exception {
        mockMvc.perform(patch("/api/users/hibernate/id/" + createdUserId).with(authenticated()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/id/" + createdUserId).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hibernation").value(true));
    }
}
