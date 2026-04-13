package com.api.tinyfarm.controller;

import com.api.tinyfarm.service.EndOfTheDayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EndOfDayControllerTest {

    @Mock
    private EndOfTheDayService endOfTheDayService;

    @InjectMocks
    private EndOfDayController endOfDayController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(endOfDayController).build();
    }

    @Test
    void shouldReturnOkWhenEndOfDayProcessingSucceeds() throws Exception {
        mockMvc.perform(post("/api/endofday/id/1"))
                .andExpect(status().isOk());

        verify(endOfTheDayService).process(1L);
    }

    @Test
    void shouldReturnNotFoundWhenEndOfDayProcessingFails() throws Exception {
        doThrow(new RuntimeException("processing failed"))
                .when(endOfTheDayService)
                .process(1L);

        mockMvc.perform(post("/api/endofday/id/1"))
                .andExpect(status().isNotFound());

        verify(endOfTheDayService).process(1L);
    }
}
