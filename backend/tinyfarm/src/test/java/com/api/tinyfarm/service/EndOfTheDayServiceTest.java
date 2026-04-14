package com.api.tinyfarm.service;

import com.api.tinyfarm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndOfTheDayServiceTest {

    @Mock
    private ChickenService chickenService;

    @Mock
    private CowService cowService;

    @Mock
    private RabbitService rabbitService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EndOfTheDayService endOfTheDayService;

    @Test
    void shouldProcessEndOfDayForOneUser() {
        Long userId = 42L;

        endOfTheDayService.process(userId);

        verify(chickenService).processEndOfDay(userId);
        verify(rabbitService).processEndOfDay(userId);
        verifyNoInteractions(cowService, userService);
    }

    @Test
    void shouldProcessScheduledForAllUsers() {
        User firstUser = new User();
        firstUser.setId(1L);
        User secondUser = new User();
        secondUser.setId(2L);

        when(userService.findAll()).thenReturn(List.of(firstUser, secondUser));

        endOfTheDayService.processScheduled();

        verify(userService).findAll();
        verify(chickenService).processEndOfDay(1L);
        verify(chickenService).processEndOfDay(2L);
        verify(rabbitService).processEndOfDay(1L);
        verify(rabbitService).processEndOfDay(2L);
        verifyNoInteractions(cowService);
    }
}
