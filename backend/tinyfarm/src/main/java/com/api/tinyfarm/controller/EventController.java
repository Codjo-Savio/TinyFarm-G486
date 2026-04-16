package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Event;
import com.api.tinyfarm.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping("/id/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id){
        ResponseEntity<Event> temp;
        try {
            temp = ResponseEntity.ok(eventService.findByUserId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
        eventService.deleteById(id);
        return temp;
    }
}
