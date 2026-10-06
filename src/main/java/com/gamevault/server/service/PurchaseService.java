package com.gamevault.server.service;

import com.gamevault.server.dto.CreatePurchaseRequest;
import com.gamevault.server.dto.LibraryGameResponse;
import com.gamevault.server.dto.PurchaseResponse;
import com.gamevault.server.exception.GameAlreadyPurchasedException;
import com.gamevault.server.exception.GameNotFoundException;
import com.gamevault.server.exception.GameUnavailableException;
import com.gamevault.server.exception.UserNotFoundException;
import com.gamevault.server.model.Game;
import com.gamevault.server.model.Purchase;
import com.gamevault.server.model.User;
import com.gamevault.server.repository.GameRepository;
import com.gamevault.server.repository.PurchaseRepository;
import com.gamevault.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            GameRepository gameRepository,
            UserRepository userRepository
    ) {
        this.purchaseRepository = purchaseRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    public PurchaseResponse purchaseGame(
            CreatePurchaseRequest request,
            String username
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() ->
                        new GameNotFoundException(request.gameId())
                );

        if(!game.isAvailable()) {
            throw new GameUnavailableException(
                    "Game is currently unavailable"
            );
        }

        if(purchaseRepository.existsByUserIdAndGameId(
                user.getId(),
                game.getId()
        )) {
            throw new GameAlreadyPurchasedException(
                    "Game was already been purchased"
            );
        }

        Purchase purchase = new Purchase(
                user,
                game,
                game.getPrice()
        );

        Purchase savedPurchase = purchaseRepository.save(purchase);

        return new PurchaseResponse(
                savedPurchase.getId(),
                game.getId(),
                game.getName(),
                savedPurchase.getPricePaid(),
                savedPurchase.getPurchasedAt()
        );
    }

    public List<LibraryGameResponse> getUserLibrary(
            String username
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException("User not found")
                );

        return purchaseRepository
                .findAllByUserId(user.getId())
                .stream()
                .map(purchase -> new LibraryGameResponse(
                        purchase.getGame().getId(),
                        purchase.getGame().getName(),
                        purchase.getGame().getDescription(),
                        purchase.getPricePaid(),
                        purchase.getPurchasedAt()
                ))
                .toList();
    }
}
