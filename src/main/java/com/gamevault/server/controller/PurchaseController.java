package com.gamevault.server.controller;

import com.gamevault.server.dto.CreatePurchaseRequest;
import com.gamevault.server.dto.PurchaseResponse;
import com.gamevault.server.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse purchaseGame(
            @Valid @RequestBody CreatePurchaseRequest request,
            Authentication authentication
    ) {
        return purchaseService.purchaseGame(
                request,
                authentication.getName()
        );
    }
}
