package com.gamevault.server.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            nullable = false,
            length = 1000
    )
    private String description;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private Genre genre;

    @Column(nullable = false)
    private boolean available;

    protected Game () {}

    public Game(
                String name,
                String description,
                BigDecimal price,
                Genre genre,
                boolean available
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.genre = genre;
        this.available = available;
    }

    public Long getId() {
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

    public void update(String name, String description, BigDecimal price, Genre genre, boolean available) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.genre = genre;
        this.available = available;
    }
}