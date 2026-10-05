package com.gamevault.server.exception;

import com.gamevault.server.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUsernameAlreadyTakenException(UsernameAlreadyTakenException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleEmailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleInvalidCredentialsException(InvalidCredentialsException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFoundException(UserNotFoundException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(CannotDeleteUserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleCannotDeleteUserException(CannotDeleteUserException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(LastAdminException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleLastAdminException(LastAdminException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(GameAlreadyPurchasedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleGameAlreadyPurchasedException(GameAlreadyPurchasedException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(GameUnavailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleGameUnavailableException(GameUnavailableException exception) {
        return exception.getMessage();
    }
}
