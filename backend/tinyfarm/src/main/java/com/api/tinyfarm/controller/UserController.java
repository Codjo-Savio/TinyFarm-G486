package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.service.UserService;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #id)")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        try {
            User user = Objects.requireNonNull(userService.findById(id));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/id/{id}")
    public ResponseEntity<String> getNameById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.findById(id).getName());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/remainingPurchases/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #id)")
    public ResponseEntity<Integer> getRemainingPurchases(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getRemainingPurchases(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> create(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.create(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #id)")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.update(id, user));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/hibernate/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #id)")
    public ResponseEntity<Void> hibernate(@PathVariable Long id) {
        try {
            userService.hibernate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #id)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
