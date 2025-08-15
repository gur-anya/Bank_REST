package com.example.bankcards.controller;

import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardRequestDTO;
import com.example.bankcards.dto.card.GetAllCardsResponseDTO;
import com.example.bankcards.dto.card.SetBalanceRequestDTO;
import com.example.bankcards.entity.enums.RoleEnum;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.security.JWTCore;
import com.example.bankcards.security.TokenFilter;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardController.class, excludeAutoConfiguration = {
    UserDetailsServiceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class CardControllerTest {

    private final UserDetailsImpl testUser = new UserDetailsImpl(1L, "user", "user@example.com", "pass", RoleEnum.USER, true);
    private final UserDetailsImpl testAdmin = new UserDetailsImpl(99L, "admin", "admin@example.com", "pass", RoleEnum.ADMIN, true);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CardService cardService;
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
    void getAllCards_whenUserIsOwner_shouldReturnPageOfCards() throws Exception {
        GetAllCardsResponseDTO response = new GetAllCardsResponseDTO(new PageImpl<>(List.of(new CardDTO())));
        doReturn(response).when(cardService).getAllCardsByUser(eq(1L), any(), any());

        mockMvc.perform(get("/api/v1/cards?page=0&size=10").with(user(testUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllCards_whenAdmin_shouldReturnPageOfCards() throws Exception {
        GetAllCardsResponseDTO response = new GetAllCardsResponseDTO(new PageImpl<>(List.of(new CardDTO())));
        doReturn(response).when(cardService).getAllCardsByUser(eq(99L), any(), any());

        mockMvc.perform(get("/api/v1/cards?page=0&size=10").with(user(testAdmin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_whenAdmin_shouldReturnCreated() throws Exception {
        CreateCardRequestDTO request = new CreateCardRequestDTO("Holder", LocalDate.now().plusYears(1), 1L);
        CardDTO response = new CardDTO();
        doReturn(response).when(cardService).createCard(any());

        mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void blockCard_whenUserIsOwner_shouldReturnOk() throws Exception {
        doReturn(new CardDTO()).when(cardService).blockCard(1L, 1L);
        mockMvc.perform(patch("/api/v1/cards/1/block")
                .with(user(testUser))
            )
            .andExpect(status().isOk());
    }

    @Test
    void blockCard_whenAdmin_shouldReturnOk() throws Exception {
        doReturn(new CardDTO()).when(cardService).blockCard(1L, 99L);
        mockMvc.perform(patch("/api/v1/cards/1/block")
                .with(user(testAdmin))
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_whenAdmin_shouldReturnOk() throws Exception {
        doReturn(new CardDTO()).when(cardService).activateCard(1L);
        mockMvc.perform(patch("/api/v1/cards/1/activate"))
            .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_whenAdminAndCardExists_shouldReturnNoContent() throws Exception {
        doNothing().when(cardService).deleteCard(1L);
        mockMvc.perform(delete("/api/v1/cards/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_whenCardNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new CardNotFoundException()).when(cardService).deleteCard(99L);
        mockMvc.perform(delete("/api/v1/cards/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setCardBalance_whenAdmin_shouldReturnOk() throws Exception {
        SetBalanceRequestDTO request = new SetBalanceRequestDTO(BigDecimal.valueOf(100));
        doReturn(new CardDTO()).when(cardService).setBalance(anyLong(), any(BigDecimal.class));

        mockMvc.perform(patch("/api/v1/cards/1/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }
}