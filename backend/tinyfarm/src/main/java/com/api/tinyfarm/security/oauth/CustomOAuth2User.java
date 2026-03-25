package com.api.tinyfarm.security.oauth;

import com.api.tinyfarm.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    private final Map<String, Object> attributes;
    private final User user;

    public CustomOAuth2User(Map<String, Object> attributes, User user) {
        this.attributes = attributes;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // pas de rôles pour l'instant
    }

    @Override
    public String getName() {
        return user.getName();
    }

    public User getUser() {
        return this.user;
    }
}