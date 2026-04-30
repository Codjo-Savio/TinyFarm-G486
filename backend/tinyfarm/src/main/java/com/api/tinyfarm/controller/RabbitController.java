package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.service.RabbitService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbits")
public class RabbitController {

    private final RabbitService rabbitService;

    public RabbitController(RabbitService rabbitService) {
        this.rabbitService = rabbitService;
    }

    // --- CRUD Operations ---
    @GetMapping("/me")
    public ResponseEntity<List<Rabbit>> getByUserId() {
        try {
            return ResponseEntity.ok(rabbitService.findByConnectedUserId());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id)")
    public ResponseEntity<Rabbit> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(rabbitService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Rabbit> create(@RequestBody Rabbit rabbit) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                rabbitService.create(rabbit)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id)")
    public ResponseEntity<Rabbit> update(
        @PathVariable Long id,
        @RequestBody Rabbit rabbit
    ) {
        try {
            return ResponseEntity.ok(rabbitService.update(id, rabbit));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            rabbitService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAll() {
        try {
            rabbitService.deleteAllRabbits();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    // --- Filters ---

    @GetMapping("/filter/name/{name}")
    public ResponseEntity<Rabbit> getByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(rabbitService.findByName(name));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/filter/type/{rabbitType}")
    public ResponseEntity<List<Rabbit>> getByRabbitType(
        @PathVariable Rabbit.RabbitTypeEnum rabbitType
    ) {
        try {
            return ResponseEntity.ok(
                rabbitService.findByRabbitType(rabbitType)
            );
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    // --- Actions ---

    @PostMapping("/{id}/feed")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id) and @securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Rabbit> feedRabbit(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(rabbitService.feedRabbit(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Example: insufficient funds
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/water")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id) and @securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Rabbit> waterRabbit(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(rabbitService.waterRabbit(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/clean")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id) and @securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Rabbit> cleanRabbit(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(rabbitService.cleanRabbit(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/heal")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id) and @securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Rabbit> healRabbit(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(rabbitService.healRabbit(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/endOfDay")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Void> processEndOfDay(@RequestParam Long userId) {
        try {
            rabbitService.processEndOfDay(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }
}
