package com.example.bankcards.exception;

public class NoUserActivenessUpdateException extends RuntimeException {
    public NoUserActivenessUpdateException(Long userId, boolean blocked) {
        super("User " + userId + " is " + (blocked ? " already blocked." : " not blocked"));
    }
}
