package com.api.tinyfarm.security.config;

import com.api.tinyfarm.security.jwt.JwtAuthenticationEntryPoint;
import com.api.tinyfarm.security.jwt.JwtRequestFilter;
import com.api.tinyfarm.security.oauth.CustomOAuth2UserService;
import com.api.tinyfarm.security.oauth.OAuth2FailureHandler;
import com.api.tinyfarm.security.oauth.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/api/auth/login/oauth2/**").permitAll()
                                .requestMatchers("/login/oauth2/**").permitAll()
                                .requestMatchers("/auth/error").permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(
                                endpoint -> endpoint.baseUri("/api/auth/login/oauth2/authorization/"))
                        .redirectionEndpoint(
                                endpoint -> endpoint.baseUri("/login/oauth2/code/*"))
                        .userInfoEndpoint(info -> info.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(
                new ClearSiteDataHeaderWriter(Directive.COOKIES));
        http.logout((logout) -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(
                        (request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                .deleteCookies("jwt", "JSESSIONID")
                .invalidateHttpSession(true)
                .addLogoutHandler(clearSiteData)
                .permitAll());
        return http.build();
    }
}
