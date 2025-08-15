package com.example.bankcards.controller;

import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.transaction.CreateTransactionRequestDTO;
import com.example.bankcards.dto.transaction.TransactionDTO;
import com.example.bankcards.entity.enums.RoleEnum;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.security.JWTCore;
import com.example.bankcards.security.TokenFilter;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.TokenService;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class, excludeAutoConfiguration = {
    UserDetailsServiceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class TransactionControllerTest {

    private final UserDetailsImpl testUser = new UserDetailsImpl(1L, "user", "user@example.com", "pass", RoleEnum.USER, true);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TransactionService transactionService;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private JWTCore jwtCore;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private TokenFilter tokenFilter;

    @Test
    void createTransaction_whenUserIsOwner_shouldReturnCreated() throws Exception {
        CreateTransactionRequestDTO request = new CreateTransactionRequestDTO(1L, 2L, BigDecimal.TEN, "Test");
        TransactionDTO response = new TransactionDTO();
        doReturn(response).when(transactionService).createTransaction(any(CreateTransactionRequestDTO.class), eq(1L));

        mockMvc.perform(post("/api/v1/transactions")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void createTransaction_whenInsufficientFunds_shouldReturnBadRequest() throws Exception {
        CreateTransactionRequestDTO request = new CreateTransactionRequestDTO(1L, 2L, BigDecimal.valueOf(1000), "Test");
        doThrow(new InsufficientFundsException()).when(transactionService).createTransaction(any(), eq(1L));

        mockMvc.perform(post("/api/v1/transactions")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactions_whenUserIsOwner_shouldReturnPage() throws Exception {
        Page<TransactionDTO> response = new PageImpl<>(List.of(new TransactionDTO()));
        doReturn(response).when(transactionService).getTransactionsByUser(eq(1L), any());

        mockMvc.perform(get("/api/v1/transactions?page=0&size=10").with(user(testUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}