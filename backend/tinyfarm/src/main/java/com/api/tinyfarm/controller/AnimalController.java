package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.service.AnimalService;
import org.springframework.http.HttpStatus;
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
        try {
            return ResponseEntity.ok(animalService.findAll());
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Animal> getById(@PathVariable Long id) {
        try{
            return ResponseEntity.ok(animalService.findById(id));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    public ResponseEntity<Animal> create(@RequestBody Animal animal) {
        try{
            return ResponseEntity.ok(animalService.create(animal));
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PutMapping("/id/{id}")
    public ResponseEntity<Animal> update(@PathVariable Long id, @RequestBody Animal animal) {
        try{
            return ResponseEntity.ok(animalService.update(id, animal));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }

    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            animalService.delete(id);
            return ResponseEntity.noContent().build();
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}
