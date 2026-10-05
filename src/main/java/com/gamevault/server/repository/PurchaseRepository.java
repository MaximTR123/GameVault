package com.gamevault.server.repository;

import com.gamevault.server.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    boolean existsByUserIdAndGameId(long userId, long gameId);

    List<Purchase> findAllByUserId(long userId, long gameId);
}
