package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users → tous les users
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    // GET /users/1 → un user par son id
    @GetMapping("/{uId}")
    public ResponseEntity<User> getById(@PathVariable Integer uId) {
        return ResponseEntity.ok(userService.findById(uId));
    }

    // POST /users → créer un user (démarre avec 1500 écus)
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.ok(userService.create(user));
    }

    // PUT /users/1 → modifier un user
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

    // PATCH /users/1/ecus?montant=8 → ajouter des écus (vente d'un oeuf)
    @PatchMapping("/{uId}/ecus/ajouter")
    public ResponseEntity<User> ajouterEcus(@PathVariable Integer uId,
                                             @RequestParam Integer montant) {
        return ResponseEntity.ok(userService.ajouterEcus(uId, montant));
    }

    // PATCH /users/1/ecus/retirer?montant=3 → retirer des écus (nourrir un animal)
    @PatchMapping("/{uId}/ecus/retirer")
    public ResponseEntity<User> retirerEcus(@PathVariable Integer uId,
                                             @RequestParam Integer montant) {
        return ResponseEntity.ok(userService.retirerEcus(uId, montant));
    }
}