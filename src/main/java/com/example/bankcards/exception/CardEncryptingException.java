package com.example.bankcards.exception;

public class CardEncryptingException extends RuntimeException {
    public CardEncryptingException(String message, Exception e) {
        super(message, e);
    }

    public CardEncryptingException(String message) {
        super(message);
    }
}
