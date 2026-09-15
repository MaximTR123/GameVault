package com.gamevault.server.model;

import java.math.BigDecimal;

public class Game {
    private final long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Genre genre;
    private final boolean available;

    public Game(long id,
                String name,
                String description,
                BigDecimal price,
                Genre genre,
                boolean available
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.genre = genre;
        this.available = available;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Genre getGenre() {
        return genre;
    }

    public boolean isAvailable() {
        return available;
    }
}