package com.gamevault.server.controller;

import com.gamevault.server.dto.RegisterRequest;
import com.gamevault.server.dto.UserResponse;
import com.gamevault.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.gamevault.server.dto.LoginRequest;
import com.gamevault.server.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return userService.login(request);
    }
}
