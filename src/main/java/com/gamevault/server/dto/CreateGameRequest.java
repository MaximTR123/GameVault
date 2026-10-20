package com.gamevault.server.dto;

import com.gamevault.server.model.Genre;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateGameRequest(
        @NotBlank(message = "Name must not be blank")
        @Size(max=150, message = "Name must be at most 150 characters")
        String name,

        @NotBlank(message = "Description must not be blank")
        @Size(max=1000, message = "Name must be at most 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.01",
                message = "Price must be at least 0.01"
        )
        BigDecimal price,

        @NotNull(message = "Genre is required")
        Genre genre
) {}