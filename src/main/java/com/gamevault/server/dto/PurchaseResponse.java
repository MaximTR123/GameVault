package com.gamevault.server.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long id,
        Long gameId,
        String gameName,
        BigDecimal pricePaid,
        LocalDateTime purchasedAt
) {
}
