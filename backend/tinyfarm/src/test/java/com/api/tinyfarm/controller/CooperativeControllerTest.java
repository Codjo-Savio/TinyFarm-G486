package com.api.tinyfarm.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.tinyfarm.service.CooperativeService;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CooperativeControllerTest extends AuthenticatedControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CooperativeService cooperativeService;

    @Test
    void shouldReturnAvailableProducts() throws Exception {
        HashMap<Long, Float> productPrices = new HashMap<>();
        productPrices.put(10L, 15.0f);

        when(cooperativeService.getAvailableProducts()).thenReturn(productPrices);

        mockMvc
            .perform(get("/api/cooperative").with(authenticated()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.10").value(15.0));
    }

    @Test
    void shouldReturnOpenState() throws Exception {
        when(cooperativeService.isOpen()).thenReturn(true);

        mockMvc
            .perform(get("/api/cooperative/isOpen").with(authenticated()))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnCurrentTime() throws Exception {
        when(cooperativeService.getTime()).thenReturn("10:15:30");

        mockMvc
            .perform(get("/api/cooperative/time").with(authenticated()))
            .andExpect(status().isOk())
            .andExpect(content().string("10:15:30"));
    }

    @Test
    void shouldDeleteCooperativeByBuyerAndDescription() throws Exception {
        mockMvc
            .perform(delete("/api/cooperative/2/Milk").with(authenticated()))
            .andExpect(status().isNoContent());

        verify(cooperativeService).deleteLessExpensiveWithDescription(2L, "Milk");
    }

    @Test
    void shouldReturnInternalServerErrorWhenGettingProductsFails() throws Exception {
        when(cooperativeService.getAvailableProducts()).thenThrow(new RuntimeException("boom"));

        mockMvc
            .perform(get("/api/cooperative").with(authenticated()))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerErrorWhenDeletionFails() throws Exception {
        doThrow(new RuntimeException("boom"))
            .when(cooperativeService)
            .deleteLessExpensiveWithDescription(2L, "Milk");

        mockMvc
            .perform(delete("/api/cooperative/2/Milk").with(authenticated()))
            .andExpect(status().isInternalServerError());
    }
}
