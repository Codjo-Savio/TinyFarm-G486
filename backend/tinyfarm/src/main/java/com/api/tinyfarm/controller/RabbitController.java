package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.service.RabbitService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/rabbits")
public class RabbitController {

    private final RabbitService rabbitService;

    public RabbitController(RabbitService rabbitService) {
        this.rabbitService = rabbitService;
    }

    // --- CRUD Operations ---

    @GetMapping
    public ResponseEntity<List<Rabbit>> getAll() {
        return ResponseEntity.ok(rabbitService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rabbit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rabbitService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Rabbit> create(@RequestBody Rabbit rabbit) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            rabbitService.create(rabbit)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rabbit> update(
        @PathVariable Long id,
        @RequestBody Rabbit rabbit
    ) {
        return ResponseEntity.ok(rabbitService.update(id, rabbit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rabbitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll() {
        rabbitService.deleteAllRabbits();
        return ResponseEntity.noContent().build();
    }

    // --- Filters ---

    @GetMapping("/filter/name/{name}")
    public ResponseEntity<Rabbit> getByName(@PathVariable String name) {
        return ResponseEntity.ok(rabbitService.findByName(name));
    }

    @GetMapping("/filter/type/{rabbitType}")
    public ResponseEntity<List<Rabbit>> getByRabbitType(
        @PathVariable Rabbit.RabbitTypeEnum rabbitType
    ) {
        return ResponseEntity.ok(rabbitService.findByRabbitType(rabbitType));
    }

    // --- Actions ---

    @PostMapping("/{id}/feed")
    public ResponseEntity<Rabbit> feedRabbit(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(rabbitService.feedRabbit(id, userId));
    }

    @PostMapping("/{id}/water")
    public ResponseEntity<Rabbit> waterRabbit(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(rabbitService.waterRabbit(id, userId));
    }

    @PostMapping("/{id}/clean")
    public ResponseEntity<Rabbit> cleanRabbit(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(rabbitService.cleanRabbit(id, userId));
    }

    @PostMapping("/{id}/heal")
    public ResponseEntity<Rabbit> healRabbit(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(rabbitService.healRabbit(id, userId));
    }

    @PostMapping("/endOfDay")
    public ResponseEntity<Void> processEndOfDay(@RequestParam Long userId) {
        rabbitService.processEndOfDay(userId);
        return ResponseEntity.ok().build();
    }
}
