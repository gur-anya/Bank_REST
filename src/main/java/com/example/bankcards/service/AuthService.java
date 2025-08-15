package com.example.bankcards.service;

import com.example.bankcards.dto.auth.LoginRequestDTO;
import com.example.bankcards.dto.auth.TokenDTO;
import com.example.bankcards.security.JWTCore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JWTCore jwtCore;

    public TokenDTO login(LoginRequestDTO loginRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getEmail(),
                loginRequestDTO.getPassword()
            )
        );
        String token = jwtCore.generateToken(authentication);

        return new TokenDTO(token);
    }
}
