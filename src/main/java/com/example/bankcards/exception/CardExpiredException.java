package com.example.bankcards.exception;

public class CardExpiredException extends RuntimeException {
    public CardExpiredException() {
        super("Card is expired");
    }
}
