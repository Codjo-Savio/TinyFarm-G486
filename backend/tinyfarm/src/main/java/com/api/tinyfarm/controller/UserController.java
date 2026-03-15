package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{uId}")
    public ResponseEntity<User> getById(@PathVariable Integer uId) {
        return ResponseEntity.ok(userService.findById(uId));
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.ok(userService.create(user));
    }

    @PutMapping("/{uId}")
    public ResponseEntity<User> update(@PathVariable Integer uId,
                                       @RequestBody User user) {
        return ResponseEntity.ok(userService.update(uId, user));
    }

    // DELETE /users/1 → supprimer un user
    @DeleteMapping("/{uId}")
    public ResponseEntity<Void> delete(@PathVariable Integer uId) {
        userService.delete(uId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /users/1/ecus?amount=8 → ajouter des écus (vente d'un oeuf)
    @PatchMapping("/ecus/add/{uId}")
    public ResponseEntity<User> addEcus(@PathVariable Integer uId,
                                             @RequestParam Integer amount) {
        return ResponseEntity.ok(userService.addEcus(uId, amount));
    }

    // PATCH /users/1/ecus/retirer?amount=3 → retirer des écus (nourrir un animal)
    @PatchMapping("/ecus/withdraw/{uId}")
    public ResponseEntity<User> withdrawEcus(@PathVariable Integer uId,
                                             @RequestParam Integer amount) {
        return ResponseEntity.ok(userService.withdrawEcus(uId, amount));
    }
}