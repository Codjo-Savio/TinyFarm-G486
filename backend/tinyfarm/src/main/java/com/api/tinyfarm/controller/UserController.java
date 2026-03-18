package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<List<User>> getAll() {

        try {
            return ResponseEntity.ok(userService.findAll());
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.findById(id));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    public ResponseEntity<User> create(@RequestBody User user) {

        try {
            return ResponseEntity.ok(userService.create(user));
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<User> update(@PathVariable Long id,
                                       @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.update(id, user));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /users/1 → supprimer un user
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.noContent().build();
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /users/1/ecus?amount=8 → ajouter des écus (vente d'un oeuf)
    @PatchMapping("/ecus/add/id/{id}")
    public ResponseEntity<User> addEcus(@PathVariable Long id,
                                             @RequestParam Integer amount) {
        try {
            return ResponseEntity.ok(userService.addEcus(id, amount));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /users/1/ecus/retirer?amount=3 → retirer des écus (nourrir un animal)
    @PatchMapping("/ecus/withdraw/id/{id}")
    public ResponseEntity<User> withdrawEcus(@PathVariable Long id,
                                             @RequestParam Integer amount) {
        try {
            return ResponseEntity.ok(userService.withdrawEcus(id, amount));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}