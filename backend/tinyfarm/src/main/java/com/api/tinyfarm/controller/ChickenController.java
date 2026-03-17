package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.service.ChickenService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chickens")
public class ChickenController{



    private final ChickenService chickenService;

    public ChickenController(ChickenService chickenService) {
        this.chickenService = chickenService;
    }

    @GetMapping
    public ResponseEntity<List<Chicken>> getAll() {
        return ResponseEntity.ok(chickenService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chicken> getById(@PathVariable Long id) {
        return ResponseEntity.ok(chickenService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Chicken> create(@RequestBody Chicken chicken) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            chickenService.create(chicken)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Chicken> update(
        @PathVariable Long id,
        @RequestBody Chicken chicken
    ) {
        return ResponseEntity.ok(chickenService.update(id, chicken));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chickenService.delete(id);
        return ResponseEntity.noContent().build();
    }
}