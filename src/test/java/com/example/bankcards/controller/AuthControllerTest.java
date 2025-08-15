package com.example.bankcards.controller;


import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.auth.CreateUserRequestDTO;
import com.example.bankcards.dto.auth.CreateUserResponseDTO;
import com.example.bankcards.dto.auth.LoginRequestDTO;
import com.example.bankcards.dto.auth.TokenDTO;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.security.JWTCore;
import com.example.bankcards.security.TokenFilter;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.TokenService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    UserDetailsServiceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @MockBean
    private UserService userService;
    @MockBean
    private TokenService tokenService;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private JWTCore jwtCore;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private TokenFilter tokenFilter;


    @Test
    void signup_whenValidRequest_shouldReturnCreated() throws Exception {
        CreateUserRequestDTO request = new CreateUserRequestDTO("Test User", "test@example.com", "Password123!");
        CreateUserResponseDTO response = new CreateUserResponseDTO(1L, "Test User", "test@example.com");
        doReturn(response).when(userService).createUser(any(CreateUserRequestDTO.class));

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void signup_whenUserAlreadyExists_shouldReturnConflict() throws Exception {
        CreateUserRequestDTO request = new CreateUserRequestDTO("Test User", "test@example.com", "Password123!");
        doThrow(new EmailAlreadyExistsException()).when(userService).createUser(any(CreateUserRequestDTO.class));

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    @Test
    void signup_whenInvalidRequest_shouldReturnBadRequest() throws Exception {
        CreateUserRequestDTO request = new CreateUserRequestDTO("", "", "123");
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_whenValidCredentials_shouldReturnToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("test@example.com", "Password123!");
        TokenDTO tokenResponse = new TokenDTO("jwt-token-string");
        doReturn(tokenResponse).when(authService).login(any(LoginRequestDTO.class));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-string"));
    }

    @Test
    void logout_whenTokenProvided_shouldBlacklistToken() throws Exception {
        String token = "jwt-token-to-blacklist";
        doNothing().when(tokenService).blacklistToken(token);

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk());

        verify(tokenService).blacklistToken(token);
    }
}