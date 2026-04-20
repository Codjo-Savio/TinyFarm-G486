package com.api.tinyfarm.security.oauth;

import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.model.Chicken.ChickenType;
import com.api.tinyfarm.model.Cow.CowType;
import com.api.tinyfarm.model.Rabbit.RabbitTypeEnum;
import com.api.tinyfarm.service.ChickenService;
import com.api.tinyfarm.service.CowService;
import com.api.tinyfarm.service.RabbitService;
import com.api.tinyfarm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final RabbitService rabbitService;
    private final CowService cowService;
    private final ChickenService chickenService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("login");

        if (email == null) {
            email = fetchPrimaryEmailFromGitHub(request.getAccessToken().getTokenValue());
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("Aucun email GitHub accessible");
        }

        boolean userAlreadyExists = userService.existsByEmail(email);
        User user = userService.findOrCreateOAuthUser(email, name, null);
        if (!userAlreadyExists) {
            createStarterResources(user);
        }

        return new CustomOAuth2User(oAuth2User.getAttributes(), user);
    }

    private void createStarterResources(User owner) {
        // 8 young rabbits
        List<String> rabbitNames = Arrays.asList("Carotte", "Panpan", "Choco", "Iris", "Napoléon", "Pinou",
                "Oréo", "Olaf");
        for (String rabbitName : rabbitNames) {
            Rabbit rabbit = new Rabbit();
            rabbit.setName(rabbitName);
            rabbit.setRabbitType(RabbitTypeEnum.lapereau);
            rabbit.setUserId(owner.getId());
            this.rabbitService.create(rabbit);
        }

        // 1 cow
        Cow cow = new Cow();
        cow.setName("Nathalie");
        cow.setCowType(CowType.D);
        cow.setUserId(owner.getId());
        this.cowService.create(cow);

        // 1 rooster
        Chicken rooster = new Chicken();
        rooster.setName("Coco");
        rooster.setChickenType(ChickenType.R);
        rooster.setUserId(owner.getId());
        this.chickenService.create(rooster);

        // 3 hens
        List<String> henNames = Arrays.asList("Marguerite", "Jocelyne", "Paulette");
        for (String henName : henNames) {
            Chicken hen = new Chicken();
            hen.setName(henName);
            hen.setChickenType(ChickenType.H);
            hen.setUserId(owner.getId());
            this.chickenService.create(hen);
        }
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
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (emails == null)
                return null;

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
