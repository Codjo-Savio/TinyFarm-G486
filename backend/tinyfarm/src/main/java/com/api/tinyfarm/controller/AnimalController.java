package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.service.AnimalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {
    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping("")
    public ResponseEntity<List<Animal>> getAll() {
        return ResponseEntity.ok(animalService.getAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Animal> getById(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.findById(id));
    }
    
    @PostMapping("")
    public ResponseEntity<Animal> create(@RequestBody Animal animal) {
        return ResponseEntity.ok(animalService.create(animal));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Animal> update(@PathVariable Long id, @RequestBody Animal animal) {
        return ResponseEntity.ok(animalService.update(id, animal));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }   
}
