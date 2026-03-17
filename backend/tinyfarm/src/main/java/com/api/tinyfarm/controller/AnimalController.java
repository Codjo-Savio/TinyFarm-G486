package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.service.AnimalService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public ResponseEntity<List<Animal>> getAll() {
        return ResponseEntity.ok(animalService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Animal> getById(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.findById(id));
    }

    @GetMapping("/uid/{uid}")
    public ResponseEntity<Animal> getByUid(@PathVariable int uid) {
        return ResponseEntity.ok(animalService.findByUid(uid));
    }

    @PostMapping
    public ResponseEntity<Animal> create(@RequestBody Animal animal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            animalService.create(animal)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Animal> update(
        @PathVariable Long id,
        @RequestBody Animal animal
    ) {
        return ResponseEntity.ok(animalService.update(id, animal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll() {
        animalService.deleteAllAnimals();
        return ResponseEntity.noContent().build();
    }

    // --- Custom Filters ---

    @GetMapping("/filter/clean/{clean}")
    public ResponseEntity<List<Animal>> getByClean(
        @PathVariable Boolean clean
    ) {
        return ResponseEntity.ok(animalService.findByClean(clean));
    }

    @GetMapping("/filter/healthy/{healthy}")
    public ResponseEntity<List<Animal>> getByHealthy(
        @PathVariable Boolean healthy
    ) {
        return ResponseEntity.ok(animalService.findByHealthy(healthy));
    }

    @GetMapping("/filter/age/{age}")
    public ResponseEntity<List<Animal>> getByAge(@PathVariable int age) {
        return ResponseEntity.ok(animalService.findByAge(age));
    }

    @GetMapping("/filter/weight/{weight}")
    public ResponseEntity<List<Animal>> getByWeight(
        @PathVariable float weight
    ) {
        return ResponseEntity.ok(animalService.findByWeight(weight));
    }

    @GetMapping("/filter/gender/{gender}")
    public ResponseEntity<List<Animal>> getByGender(
        @PathVariable Boolean gender
    ) {
        return ResponseEntity.ok(animalService.findByGender(gender));
    }
}
