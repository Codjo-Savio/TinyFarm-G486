package com.api.tinyfarm.authentication;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.security.jwt.JwtProviderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.time.Instant;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtRequestFilterTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtProviderConfig jwtProviderConfig;

    private String generateToken(Long userId){
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        return  jwtProviderConfig.jwtEncoder().encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Test
    void shouldAuthorizeRequestWithValidToken() throws Exception{
        User user = new User(1L, "test", "usertest@gmail.com", User.Gender.M, 1500, false, 1);
        String token = generateToken(user.getId());
        mockMvc.perform(
                get("/api/me")
                        .header("Authorization", "Bearer" + token)
        ).andExpect(status().isOk());

    }


}
