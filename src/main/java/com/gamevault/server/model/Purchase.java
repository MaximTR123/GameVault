package com.gamevault.server.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "purchases",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"})
)
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private BigDecimal pricePaid;

    @Column(nullable = false)
    private LocalDateTime purchasedAt;

    protected Purchase() {

    }

    public Purchase(User user, Game game, BigDecimal pricePaid) {
        this.user = user;
        this.game = game;
        this.pricePaid = pricePaid;
        this.purchasedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Game getGame() {
        return game;
    }

    public BigDecimal getPricePaid() {
        return pricePaid;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }
}
