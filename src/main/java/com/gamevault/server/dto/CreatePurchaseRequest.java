package com.gamevault.server.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePurchaseRequest(
        @NotNull(message = "Game ID is required")
        Long gameId
) {
}
