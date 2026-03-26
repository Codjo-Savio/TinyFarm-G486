package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<User> authenticateUser(@AuthenticationPrincipal UserDetails userDetails){
            try{
                return ResponseEntity.ok(userService.getByEmail(userDetails.getUsername()));
            } catch (Exception e) {
                return ResponseEntity.notFound().build();
            }
    }

    @GetMapping("me")
    public ResponseEntity<User> getUser(@AuthenticationPrincipal UserDetails userDetails){
        try{
            return ResponseEntity.ok(userService.getByEmail(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
