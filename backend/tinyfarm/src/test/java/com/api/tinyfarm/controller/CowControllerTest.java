package com.api.tinyfarm.controller;
import com.api.tinyfarm.service.CowService;
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
public class CowControllerTest extends AuthenticatedControllerTestSupport {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    CowService cowService;
    @BeforeEach
    void setup() throws Exception {
        cowService.deleteAll();
        String json = """
                {
                    "id" : 1,
                    "cowType" : "D",
                    "name" : "Marguerite",
                    "milking" : false
                }
                """;
        mockMvc.perform(
                post("/api/cows")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());
    }
    @Test
    void shouldCreateCow() throws Exception {
        String json = """
                {
                    "id" : 1,
                    "cowType" : "B",
                    "name" : "Bovino",
                    "milking" : false
                }
                """;
        mockMvc.perform(
                        post("/api/cows")
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnAllCows() throws Exception {
        mockMvc.perform(get("/api/cows").with(authenticated()))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnCowByName() throws Exception {
        mockMvc.perform(get("/api/cows/name/Marguerite").with(authenticated()))
                .andExpect(status().isOk());
    }
    @Test
    void cowShouldNotBeFoundByName() throws Exception {
        mockMvc.perform(get("/api/cows/name/unknown").with(authenticated()))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldDeleteCowById() throws Exception {
        Long id = cowService.getByName("Marguerite").getId();
        mockMvc.perform(delete("/api/cows/id/" + id).with(authenticated()))
                .andExpect(status().isNoContent());
    }
}
