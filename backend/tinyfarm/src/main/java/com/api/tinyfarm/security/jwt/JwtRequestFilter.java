package com.api.tinyfarm.security.jwt;

import com.api.tinyfarm.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    // we read the token and extract the username and authorities, then we validate or invalidate
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain){
        try{
            // reading the token
            String header = request.getHeader("Authorization");
            if(header != null && header.startsWith("Bearer ")){
                String token = header.substring(7);
                Jwt decoded = jwtDecoder.decode(token);

                Long userId = Long.valueOf(decoded.getSubject());

                var userDetails = userService.findById(userId);

                // extracting the user details
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                      userDetails, null, List.of()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }catch (Exception ignored) {}

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
