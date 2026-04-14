package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.api.tinyfarm.model.Event;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    Event event;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();

        event = new Event();
        event.setUserId(12L);
        event.setText("Attention test !");

        eventRepository.save(event);
    }

    @Test
    void shouldSaveEvent() {
        Event eventSaveTest = new Event();
        eventSaveTest.setUserId(3L);
        eventSaveTest.setText("Attention test save !");
        eventRepository.save(eventSaveTest);

        Optional<Event> found = eventRepository.findById(eventSaveTest.getId());

        assertTrue(found.isPresent());
        assertEquals(eventSaveTest.getId(), found.get().getId());
        assertEquals(3L, found.get().getUserId());
        assertEquals("Attention test save !", found.get().getText());
    }

    @Test
    void shouldFindById() {
        Optional<Event> found = eventRepository.findById(event.getId());

        assertTrue(found.isPresent());
        assertEquals(event.getId(), found.get().getId());
        assertEquals(event.getText(), found.get().getText());
    }

    @Test
    void shouldFindByUserId() {
        Optional<Event> found = eventRepository.findByUserId(event.getUserId());

        assertTrue(found.isPresent());
        assertEquals(event.getUserId(), found.get().getUserId());
        assertEquals(event.getText(), found.get().getText());
    }
}
