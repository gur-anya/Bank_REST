package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CardFilterDTO;
import com.example.bankcards.dto.card.CreateCardRequestDTO;
import com.example.bankcards.dto.card.GetAllCardsResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.RoleEnum;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberUtil;
import com.example.bankcards.util.CardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    public GetAllCardsResponseDTO getAllCardsByUser(Long userId, Pageable pageable, CardFilterDTO filterDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);
        Specification<Card> spec = Specification.where(null);
        if (user.getRole() != RoleEnum.ADMIN) {
            spec = spec.and(CardSpecification.hasUserId(userId));
        }

        if (filterDTO != null) {
            spec = spec.and(CardSpecification.hasStatus(filterDTO.getStatus()));
            spec = spec.and(CardSpecification.balanceIsMoreThan(filterDTO.getBalanceMoreThan()));
            spec = spec.and(CardSpecification.balanceIsLessThan(filterDTO.getBalanceLessThan()));
            spec = spec.and(CardSpecification.expiryDateIsAfter(filterDTO.getExpiryDateFrom()));
            spec = spec.and(CardSpecification.expiryDateIsBefore(filterDTO.getExpiryDateTo()));
            spec = spec.and(CardSpecification.cardHolderLike(filterDTO.getCardHolder()));
        }
        Page<Card> cards = cardRepository.findAll(spec, pageable);

        Page<CardDTO> cardDTOs = cards.map(cardMapper::toDTO);
        return new GetAllCardsResponseDTO(cardDTOs);
    }

    @Transactional
    public CardDTO createCard(CreateCardRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
            .orElseThrow(UserNotFoundException::new);

        String plainCardNumber = CardNumberUtil.generateCardNumber();

        byte[] encryptedCardNumberBytes = CardNumberUtil.encryptCardNumber(plainCardNumber);

        Card card = new Card();
        card.setCardNumber(encryptedCardNumberBytes);
        card.setCardHolder(requestDTO.getCardHolder());
        card.setExpiryDate(requestDTO.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(java.math.BigDecimal.ZERO);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        return cardMapper.toDTO(savedCard);
    }

    @Transactional
    public CardDTO blockCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
            .orElseThrow(CardNotFoundException::new);


        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (user.getRole() != RoleEnum.ADMIN && !card.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access to card denied");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        return cardMapper.toDTO(savedCard);
    }

    @Transactional
    public CardDTO activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
            .orElseThrow(CardNotFoundException::new);

        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        return cardMapper.toDTO(savedCard);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException();
        }
        cardRepository.deleteById(cardId);
    }

    public Card findById(Long cardId) {
        return cardRepository.findById(cardId)
            .orElseThrow(CardNotFoundException::new);
    }

    public Card findByIdAndUserId(Long cardId, Long userId) {
        Card card = findById(cardId);
        if (!card.getUser().getId().equals(userId)) {
            throw new CardNotFoundException();
        }
        return card;
    }

    @Transactional
    public CardDTO setBalance(Long cardId, BigDecimal newBalance) {
        Card card = cardRepository.findById(cardId)
            .orElseThrow(CardNotFoundException::new);

        card.setBalance(newBalance);
        Card savedCard = cardRepository.save(card);
        return cardMapper.toDTO(savedCard);
    }
}
