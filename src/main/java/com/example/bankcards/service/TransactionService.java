package com.example.bankcards.service;

import com.example.bankcards.dto.transaction.CreateTransactionRequestDTO;
import com.example.bankcards.dto.transaction.TransactionDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardExpiredException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardService cardService;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;

    @Transactional
    public TransactionDTO createTransaction(CreateTransactionRequestDTO requestDTO, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);

        Card fromCard = cardService.findByIdAndUserId(requestDTO.getFromCardId(), user.getId());
        Card toCard = cardService.findByIdAndUserId(requestDTO.getToCardId(), user.getId());

        validateCardForTransaction(fromCard);
        validateCardForTransaction(toCard);

        if (fromCard.getBalance().compareTo(requestDTO.getAmount()) < 0) {
            throw new InsufficientFundsException();
        }

        fromCard.setBalance(fromCard.getBalance().subtract(requestDTO.getAmount()));
        toCard.setBalance(toCard.getBalance().add(requestDTO.getAmount()));

        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(requestDTO.getAmount());
        transaction.setDescription(requestDTO.getDescription());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(savedTransaction);
    }

    public Page<TransactionDTO> getTransactionsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);
        Page<Transaction> transactions = transactionRepository.findByUserId(user.getId(), pageable);
        return transactions.map(transactionMapper::toDTO);
    }

    private void validateCardForTransaction(Card card) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardBlockedException();
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpiredException();
        }
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CardExpiredException();
        }
    }
}
