package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Event;
import com.api.tinyfarm.repository.EventRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.management.RuntimeErrorException;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Create method

    public Event create(Event event) {
        return eventRepository.save(event);
    }

    // Update method

    public Event update(Long id, Event updatedEvent) {
        Event existing = eventRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Event introuvable : " + id)
            );
        existing.setUserId(updatedEvent.getUserId());
        existing.setText(updatedEvent.getText());

        return eventRepository.save(existing);
    }

    // Find methods

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Event findById(Long id) {
        return eventRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Event introuvable : " + id)
            );
    }

    public Event findByUserId(Long userId) {
        return eventRepository
            .findByUserId(userId)
            .orElseThrow(() ->
                new RuntimeException("Event introuvable : " + userId)
            );
    }

    // Delete methods

    public void deleteById(Long id) {
        eventRepository.deleteById(id);
    }

    public void deleteByUserId(Long userId) {
        eventRepository.deleteByUserId(userId);
    }

    public void deleteAll() {
        eventRepository.deleteAll();
    }
}
