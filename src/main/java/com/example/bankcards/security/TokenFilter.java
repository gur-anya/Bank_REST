package com.example.bankcards.security;

import com.example.bankcards.dto.ErrorResponse;
import com.example.bankcards.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {
    private final JWTCore jwtCore;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = null;
        String email = null;
        UserDetails userDetails = null;
        UsernamePasswordAuthenticationToken auth = null;
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            jwt = headerAuth.substring(7);
        } else {
            log.warn("Invalid or missing Authorization header");
        }

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            jwt = headerAuth.substring(7);
            log.debug("Extracted JWT: {}", jwt);
        }

        if (jwt != null) {
            if (tokenService.isTokenBlacklisted(jwt)) {
                log.warn("Token is blacklisted: {}", jwt);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token is invalid, please log in again");
                return;
            }
            try {
                email = jwtCore.getEmailFromJwt(jwt);
                log.debug("Extracted email from JWT: {}", email);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userDetails = userDetailsService.loadUserByUsername(email);
                    log.debug("Loaded UserDetails: {}", userDetails.getUsername());
                    auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException e) {
                log.warn("Expired JWT: {}", e.getMessage());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token is expired, please log in again");
                return;
            } catch (MalformedJwtException e) {
                log.warn("Malformed JWT: {}", e.getMessage());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Malformed JWT");
                return;
            } catch (Exception e) {
                log.error("Authentication error: {}", e.getMessage(), e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication error");
                return;
            }
        } else {
            log.debug("No valid JWT found in request");
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status.value());
        new ObjectMapper().writeValue(response.getOutputStream(), new ErrorResponse(message, LocalDateTime.now()));
    }
}
