package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping("")
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.ok(userService.create(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Integer id,
                                       @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    // DELETE /users/1 → supprimer un user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /users/1/ecus?amount=8 → ajouter des écus (vente d'un oeuf)
    @PatchMapping("/ecus/add/{id}")
    public ResponseEntity<User> addEcus(@PathVariable Integer id,
                                             @RequestParam Integer amount) {
        return ResponseEntity.ok(userService.addEcus(id, amount));
    }

    // PATCH /users/1/ecus/retirer?amount=3 → retirer des écus (nourrir un animal)
    @PatchMapping("/ecus/withdraw/{id}")
    public ResponseEntity<User> withdrawEcus(@PathVariable Integer id,
                                             @RequestParam Integer amount) {
        return ResponseEntity.ok(userService.withdrawEcus(id, amount));
    }
}