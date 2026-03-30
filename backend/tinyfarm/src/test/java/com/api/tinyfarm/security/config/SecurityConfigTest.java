package com.api.tinyfarm.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfigTest.TestEndpoints.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowPublicEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/test"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/secure/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUserOnProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/secure/test")
                        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidNonAdminUserOnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminUserOnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @RestController
    @TestConfiguration
    static class TestEndpoints {

        @GetMapping("/api/public/test")
        ResponseEntity<String> publicEndpoint() {
            return ResponseEntity.ok("public");
        }

        @GetMapping("/api/secure/test")
        ResponseEntity<String> secureEndpoint() {
            return ResponseEntity.ok("secure");
        }

        @GetMapping("/api/admin/test")
        ResponseEntity<String> adminEndpoint() {
            return ResponseEntity.ok("admin");
        }
    }
}
