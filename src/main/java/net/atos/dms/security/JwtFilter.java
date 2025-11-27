package net.atos.dms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;

    @Autowired(required = false)
    private UserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    Object principal;
                    Object authorities = Collections.emptyList();

                    if (userDetailsService != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        principal = userDetails;
                        authorities = userDetails.getAuthorities();
                    } else {
                        principal = username;
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, (java.util.Collection) authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user from JWT: {} for request {}", username, request.getRequestURI());
                } else {
                    log.debug("Token failed validation for request {}", request.getRequestURI());
                }
            } catch (Exception ex) {
                log.debug("JWT processing failed: {}", ex.getMessage());
            }
        } else {
            log.debug("No Bearer Authorization header present for request {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
