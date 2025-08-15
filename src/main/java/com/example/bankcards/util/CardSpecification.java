package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardSpecification {


    public static Specification<Card> hasUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, builder) -> {
            Join<Card, User> userJoin = root.join("user");
            return builder.equal(userJoin.get("id"), userId);
        };
    }


    public static Specification<Card> hasStatus(CardStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }


    public static Specification<Card> balanceIsMoreThan(BigDecimal balance) {
        if (balance == null) {
            return null;
        }
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("balance"), balance);
    }


    public static Specification<Card> balanceIsLessThan(BigDecimal balance) {
        if (balance == null) {
            return null;
        }
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("balance"), balance);
    }

    public static Specification<Card> expiryDateIsAfter(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("expiryDate"), date);
    }

    public static Specification<Card> expiryDateIsBefore(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("expiryDate"), date);
    }


    public static Specification<Card> cardHolderLike(String cardHolderName) {
        if (!StringUtils.hasText(cardHolderName)) {
            return null;
        }
        return (root, query, builder) ->
            builder.like(builder.lower(root.get("cardHolder")), "%" + cardHolderName.toLowerCase() + "%");
    }
}