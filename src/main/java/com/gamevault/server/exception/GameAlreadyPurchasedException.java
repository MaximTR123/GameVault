package com.gamevault.server.exception;

public class GameAlreadyPurchasedException extends RuntimeException {
    public GameAlreadyPurchasedException(String message) {
        super(message);
    }
}
