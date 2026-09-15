package com.gamevault.server.dto;

import com.gamevault.server.model.Genre;

import java.math.BigDecimal;

public record GameResponse(
        long id,

        String name,

        String description,

        BigDecimal price,

        Genre genre,

        boolean available
) {
}
