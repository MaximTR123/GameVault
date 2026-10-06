package com.gamevault.server.controller;

import com.gamevault.server.dto.LibraryGameResponse;
import com.gamevault.server.service.PurchaseService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class LibraryController {
    private final PurchaseService purchaseService;

    public LibraryController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<LibraryGameResponse> getLibrary(Authentication authentication) {
        return purchaseService.getUserLibrary(
                authentication.getName()
        );
    }
}
