package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.api.tinyfarm.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class EventServiceTest {

    @Autowired
    EventService eventService;

    private Event event;
    private Long eventId;
    private Long eventUserId;
    private String eventText;

    @BeforeEach
    void setUp() {
        eventService.deleteAll();
        eventUserId = 12L;
        eventText = "Votre vache à quelques problèmes intestinaux !";

        event = new Event();
        event.setUserId(eventUserId);
        event.setText(eventText);

        event = eventService.create(event);
        eventId = event.getId();
    }

    @Test
    void shouldCreateEvent() {
        Event found = eventService.create(event);

        assertNotNull(found.getId());
        assertEquals(eventId, found.getId());
        assertEquals(eventUserId, 12L);
        assertEquals(eventText, found.getText());
    }

    @Test
    void shouldFindById() {
        Event found = eventService.findById(eventId);

        assertNotNull(found.getId());
        assertEquals(eventId, found.getId());
        assertEquals(eventUserId, 12L);
        assertEquals(eventText, found.getText());
    }

    @Test
    void shouldFindByUserId() {
        Event found = eventService.findByUserId(eventUserId);

        assertNotNull(found.getId());
        assertEquals(eventId, found.getId());
        assertEquals(eventUserId, 12L);
        assertEquals(eventText, found.getText());
    }

    @Test
    void shouldDeleteById() {
        eventService.deleteById(eventId);
        assertEquals(0, eventService.findAll().size());
    }

    @Test
    void shouldDeleteByUserId() {
        eventService.deleteByUserId(eventUserId);
        assertEquals(0, eventService.findAll().size());
    }

    @Test
    void shouldDeleteAll() {
        eventService.deleteAll();
        assertEquals(0, eventService.findAll().size());
    }
}
