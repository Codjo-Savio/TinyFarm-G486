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
        try {
            return ResponseEntity.ok(chickenService.findAll());
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chicken> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(chickenService.findById(id));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<Chicken> getByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(chickenService.getByName(name));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Chicken> create(@RequestBody Chicken chicken) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    chickenService.create(chicken)
            );
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<Chicken> update(@PathVariable Long id, @RequestBody Chicken chicken) {
        try {
            return ResponseEntity.ok(chickenService.update(id, chicken));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            chickenService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

    }
}