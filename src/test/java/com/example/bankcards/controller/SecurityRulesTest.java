package com.example.bankcards.controller;
import com.example.bankcards.config.TestAppConfig;
import com.example.bankcards.dto.card.CreateCardRequestDTO;
import com.example.bankcards.security.JWTCore;
import com.example.bankcards.security.TokenFilter;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.time.LocalDate;
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestAppConfig.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JWTCore jwtCore;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private TokenFilter tokenFilter;


    @Test
    @WithMockUser(roles = "USER")
    void createCard_whenUser_shouldReturnForbidden() throws Exception {
        CreateCardRequestDTO request = new CreateCardRequestDTO("Holder", LocalDate.now(), 1L);
        mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_whenUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllCards_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cards"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getTransactions_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/transactions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isUnauthorized());
    }
}