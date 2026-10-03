package com.gamevault.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatusController {
    @GetMapping("/api/status")
    public Map<String, String> getStatus() {
        return Map.of(
                "status", "online",
                "application", "GameVault"
        );
    }

    @GetMapping("/api/status/private")
    public String privateStatus() {
        return "You are authenticated";
    }
}
