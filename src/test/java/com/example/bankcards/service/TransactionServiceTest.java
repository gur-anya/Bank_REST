package com.example.bankcards.service;


import com.example.bankcards.dto.transaction.CreateTransactionRequestDTO;
import com.example.bankcards.dto.transaction.TransactionDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardService cardService;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TransactionService transactionService;
    @Mock
    private TransactionMapper transactionMapper;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(testUser);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(500));
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(testUser);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(100));
        toCard.setExpiryDate(LocalDate.now().plusYears(1));
    }

    @Test
    void createTransaction_whenValid_shouldSucceed() {
        CreateTransactionRequestDTO request = new CreateTransactionRequestDTO(1L, 2L, BigDecimal.valueOf(100), "test");
        when(transactionMapper.toDTO(any(Transaction.class))).thenReturn(new TransactionDTO());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardService.findByIdAndUserId(1L, 1L)).thenReturn(fromCard);
        when(cardService.findByIdAndUserId(2L, 1L)).thenReturn(toCard);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(request, 1L);

        assertEquals(BigDecimal.valueOf(400), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(200), toCard.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_whenInsufficientFunds_shouldThrowException() {
        CreateTransactionRequestDTO request = new CreateTransactionRequestDTO(1L, 2L, BigDecimal.valueOf(1000), "test");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardService.findByIdAndUserId(1L, 1L)).thenReturn(fromCard);
        when(cardService.findByIdAndUserId(2L, 1L)).thenReturn(toCard);

        assertThrows(InsufficientFundsException.class, () -> transactionService.createTransaction(request, 1L));
    }

    @Test
    void createTransaction_whenCardIsBlocked_shouldThrowException() {
        fromCard.setStatus(CardStatus.BLOCKED);
        CreateTransactionRequestDTO request = new CreateTransactionRequestDTO(1L, 2L, BigDecimal.valueOf(100), "test");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardService.findByIdAndUserId(1L, 1L)).thenReturn(fromCard);
        when(cardService.findByIdAndUserId(2L, 1L)).thenReturn(toCard);

        assertThrows(CardBlockedException.class, () -> transactionService.createTransaction(request, 1L));
    }

    @Test
    void getTransactionsByUser_whenUserExists_shouldReturnPage() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        transactionService.getTransactionsByUser(1L, Pageable.unpaged());

        verify(transactionRepository).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void getTransactionsByUser_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> transactionService.getTransactionsByUser(1L, Pageable.unpaged()));
    }
}