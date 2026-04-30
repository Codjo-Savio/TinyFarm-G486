package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.service.ChickenService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chickens")
public class ChickenController {

    private final ChickenService chickenService;

    public ChickenController(ChickenService chickenService) {
        this.chickenService = chickenService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<Chicken>> getByUserId() {
        try {
            return ResponseEntity.ok(chickenService.findByConnectedUserId());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/eggs")
    public ResponseEntity<Integer> getEggsNumber() {
        try {
            return ResponseEntity.ok(chickenService.getEggNumber());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id)")
    public ResponseEntity<Chicken> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(chickenService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Chicken> getByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(chickenService.getByName(name));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Chicken> create(@RequestBody Chicken chicken) {
        try {
            return ResponseEntity.ok(chickenService.create(chicken));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PutMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id)")
    public ResponseEntity<Chicken> update(
        @PathVariable Long id,
        @RequestBody Chicken chicken
    ) {
        try {
            return ResponseEntity.ok(chickenService.update(id, chicken));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            chickenService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/name/{name}")
    public ResponseEntity<Void> deleteByName(@PathVariable String name) {
        try {
            chickenService.deleteByName(name);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Actions ---

    @PostMapping("/{id}/feed")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id) and @securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Chicken> feedChicken(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(chickenService.feedChicken(id, userId));
    }

    @PostMapping("/{id}/water")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id) and @securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Chicken> waterChicken(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(chickenService.waterChicken(id, userId));
    }

    @PostMapping("/{id}/clean")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id) and @securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Chicken> cleanChicken(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(chickenService.cleanChicken(id, userId));
    }

    @PostMapping("/{id}/heal")
    @PreAuthorize("@securityAuthorizationService.ownsAnimal(authentication, #id) and @securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Chicken> healChicken(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(chickenService.healChicken(id, userId));
    }

    @PostMapping("/endOfDay")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Void> processEndOfDay(@RequestParam Long userId) {
        chickenService.processEndOfDay(userId);
        return ResponseEntity.ok().build();
    }
}
