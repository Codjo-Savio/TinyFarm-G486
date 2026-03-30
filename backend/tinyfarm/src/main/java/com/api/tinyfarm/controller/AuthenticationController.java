package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.security.oauth.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {
    @PostMapping("/login")
    public ResponseEntity<User> authenticateUser(@AuthenticationPrincipal Object principal){
        return resolveAuthenticatedUser(principal);
    }

    @GetMapping("me")
    public ResponseEntity<User> getUser(@AuthenticationPrincipal Object principal){
        return resolveAuthenticatedUser(principal);
    }

    private ResponseEntity<User> resolveAuthenticatedUser(Object principal) {
        if (principal instanceof User user) {
            return ResponseEntity.ok(user);
        }
        if (principal instanceof CustomOAuth2User oAuth2User) {
            return ResponseEntity.ok(oAuth2User.getUser());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
