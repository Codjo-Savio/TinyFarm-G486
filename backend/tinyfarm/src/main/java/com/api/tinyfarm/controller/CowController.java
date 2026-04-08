package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.service.CowService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cows")
public class CowController {

    private final CowService cowService;

    public CowController(CowService cowService) {
        this.cowService = cowService;
    }

    @GetMapping
    public ResponseEntity<List<Cow>> getAll() {
        try {
            return ResponseEntity.ok(cowService.findAll());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Cow> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cowService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Cow> getByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(cowService.getByName(name));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    public ResponseEntity<Cow> create(@RequestBody Cow cow) {
        try {
            return ResponseEntity.ok(cowService.create(cow));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<Cow> update(
        @PathVariable Long id,
        @RequestBody Cow cow
    ) {
        try {
            return ResponseEntity.ok(cowService.update(id, cow));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            cowService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/name/{name}")
    public ResponseEntity<Void> deleteByName(@PathVariable String name) {
        try {
            cowService.deleteByName(name);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Actions ---

    @PostMapping("/{id}/feed")
    public ResponseEntity<Cow> feedCow(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(cowService.feedCow(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/water")
    public ResponseEntity<Cow> waterCow(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(cowService.waterCow(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/clean")
    public ResponseEntity<Cow> cleanCow(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(cowService.cleanCow(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/heal")
    public ResponseEntity<Cow> healCow(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(cowService.healCow(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/endOfDay")
    public ResponseEntity<Void> processEndOfDay(@RequestParam Long userId) {
        try {
            cowService.processEndOfDay(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }
}
