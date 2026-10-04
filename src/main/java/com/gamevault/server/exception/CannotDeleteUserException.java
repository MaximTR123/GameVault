package com.gamevault.server.exception;

public class CannotDeleteUserException extends RuntimeException {
    public CannotDeleteUserException(String message) {
        super(message);
    }
}
