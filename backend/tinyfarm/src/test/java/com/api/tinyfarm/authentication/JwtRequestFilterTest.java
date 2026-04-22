package com.api.tinyfarm.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.security.jwt.JwtRequestFilter;
import com.api.tinyfarm.service.UserService;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class JwtRequestFilterTest {

    private final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    private final UserService userService = mock(UserService.class);
    private final JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(
        jwtDecoder,
        userService
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthorizeRequestWithValidToken()
        throws ServletException, IOException {
        User user = new User(
            1L,
            "test",
            "usertest@gmail.com",
            User.Gender.M,
            1500F,
            false,
            null,
            1
        );
        Jwt jwt = new Jwt(
            "valid-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            java.util.Map.of("alg", "HS256"),
            java.util.Map.of("sub", "1")
        );

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        when(userService.findById(1L)).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtRequestFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
            user,
            SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
        );
        verify(jwtDecoder).decode("valid-token");
        verify(userService).findById(1L);
    }
}
