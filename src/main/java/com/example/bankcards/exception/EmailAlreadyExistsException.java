package com.example.bankcards.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("User with this email already exists");
    }
}
