package com.gamevault.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GameNotFoundException extends RuntimeException{
    public GameNotFoundException(long id) {
        super(
                "Game with id " +
                id +
                " was not found"
        );
    }
}
