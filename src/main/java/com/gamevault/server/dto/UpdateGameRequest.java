package com.gamevault.server.dto;

import com.gamevault.server.model.Genre;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateGameRequest(
        @NotBlank(message = "Name must not be blank")
        String name,

        @NotBlank(message = "Description must not be blank")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.01",
                message = "Price must be at least 0.01"
        )
        BigDecimal price,

        @NotNull(message = "Genre is required")
        Genre genre,

        @NotNull(message = "Avaliable is required")
        Boolean available
) {
}
