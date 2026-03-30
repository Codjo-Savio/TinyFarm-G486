package com.api.tinyfarm.security.oauth;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

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
            email = fetchPrimaryEmailFromGitHub(request.getAccessToken().getTokenValue());
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("Aucun email GitHub accessible");
        }

        User user = userService.findOrCreateOAuthUser(email, name, null);

        return new CustomOAuth2User(oAuth2User.getAttributes(), user);
    }


    // trying to get the email with https://api.github.com/user/emails
    private String fetchPrimaryEmailFromGitHub(String accessToken) {
        try {
            RestClient client = RestClient.create();
            List<Map<String, Object>> emails = client.get()
                    .uri("https://api.github.com/user/emails")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (emails == null) return null;

            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("verified"))
                            && Boolean.TRUE.equals(e.get("primary")))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            System.out.println("Impossible de récupérer les emails GitHub : {}" + e.getMessage());
            return null;
        }
    }
}
