package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.RoleEnum;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;
    @InjectMocks
    private CardService cardService;

    private User testUser;
    private User testAdmin;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setRole(RoleEnum.USER);

        testAdmin = new User();
        testAdmin.setId(99L);
        testAdmin.setRole(RoleEnum.ADMIN);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setUser(testUser);
        testCard.setStatus(CardStatus.ACTIVE);
    }

    @Test
    void getAllCardsByUser_whenUserIsAdmin_shouldCallFindAll() {
        when(userRepository.findById(99L)).thenReturn(Optional.of(testAdmin));
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        cardService.getAllCardsByUser(99L, Pageable.unpaged(), null);

        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllCardsByUser_whenUserIsRegular_shouldFilterByUserId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        cardService.getAllCardsByUser(1L, Pageable.unpaged(), null);

        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void createCard_whenUserExists_shouldCreateAndSaveCard() {
        CreateCardRequestDTO request = new CreateCardRequestDTO("Holder", LocalDate.now(), 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));
        when(cardMapper.toDTO(any(Card.class))).thenReturn(new CardDTO());

        cardService.createCard(request);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());
        assertEquals("Holder", cardCaptor.getValue().getCardHolder());
    }

    @Test
    void createCard_whenUserNotFound_shouldThrowException() {
        CreateCardRequestDTO request = new CreateCardRequestDTO("Holder", LocalDate.now(), 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.createCard(request));
    }

    @Test
    void blockCard_whenAdminBlocks_shouldBlockCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(userRepository.findById(99L)).thenReturn(Optional.of(testAdmin));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.blockCard(1L, 99L);

        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
    }

    @Test
    void blockCard_whenUserBlocksOwnCard_shouldBlockCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.blockCard(1L, 1L);

        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
    }

    @Test
    void blockCard_whenUserBlocksAnotherUserCard_shouldThrowAccessDenied() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setRole(RoleEnum.USER);
        testCard.setUser(anotherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(AccessDeniedException.class, () -> cardService.blockCard(1L, 1L));
    }

    @Test
    void activateCard_whenCardExists_shouldActivateCard() {
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, testCard.getStatus());
    }

    @Test
    void deleteCard_whenCardExists_shouldDeleteCard() {
        when(cardRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cardRepository).deleteById(1L);

        cardService.deleteCard(1L);

        verify(cardRepository).deleteById(1L);
    }

    @Test
    void deleteCard_whenCardNotFound_shouldThrowException() {
        when(cardRepository.existsById(1L)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(1L));
    }

    @Test
    void setBalance_whenCardExists_shouldUpdateBalance() {
        BigDecimal newBalance = BigDecimal.valueOf(100.00);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.setBalance(1L, newBalance);

        assertEquals(newBalance, testCard.getBalance());
    }
}