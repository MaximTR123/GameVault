package com.gamevault.server.exception;

public class GameHasPurchasesException extends RuntimeException {
    public GameHasPurchasesException(long gameId) {
        super("Game with id " + gameId + "has purchases and cannot be deleted");
    }
}
