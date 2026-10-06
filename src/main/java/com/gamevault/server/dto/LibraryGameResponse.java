package com.gamevault.server.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LibraryGameResponse(
        Long gameId,
        String gameName,
        String description,
        BigDecimal pricePaid,
        LocalDateTime purchasedAt
) {
}
