package com.api.tinyfarm.security.jwt;

import com.api.tinyfarm.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);
    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ") &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                String token = header.substring(7);
                Jwt decoded = jwtDecoder.decode(token);
                Long userId = Long.valueOf(decoded.getSubject());
                var userDetails = userService.findById(userId);
                Collection<? extends GrantedAuthority> authorities = extractAuthorities(decoded);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.debug("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object rolesClaim = jwt.getClaims().get("roles");
        if (rolesClaim instanceof Collection<?> roles) {
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(this::normalizeRole)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            if (!authorities.isEmpty()) {
                return authorities;
            }
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

}
