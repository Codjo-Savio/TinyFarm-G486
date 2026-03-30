package com.api.tinyfarm.security.oauth;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("GitHub n'a pas renvoyé d'email");
        }

        User user = userService.findOrCreateOAuthUser(email, name, null);

        return new CustomOAuth2User(oAuth2User.getAttributes(), user);
    }
}
