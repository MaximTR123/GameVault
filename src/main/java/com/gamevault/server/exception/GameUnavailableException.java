package com.gamevault.server.exception;

public class GameUnavailableException extends RuntimeException {
    public GameUnavailableException(String message) {
        super(message);
    }
}
