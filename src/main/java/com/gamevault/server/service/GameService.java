package com.gamevault.server.service;

import com.gamevault.server.dto.CreateGameRequest;
import com.gamevault.server.dto.GameResponse;
import com.gamevault.server.dto.UpdateGameRequest;
import com.gamevault.server.exception.GameHasPurchasesException;
import com.gamevault.server.exception.GameNotFoundException;
import com.gamevault.server.model.Game;
import com.gamevault.server.repository.GameRepository;
import com.gamevault.server.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PurchaseRepository purchaseRepository;

    public GameService(
            GameRepository gameRepository,
            PurchaseRepository purchaseRepository
    ) {

        this.gameRepository = gameRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public List<GameResponse> getAll() {

        return gameRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GameResponse getById(long id) {

        Game game = gameRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new GameNotFoundException(id)
                );

        return toResponse(game);
    }

    public GameResponse create(
            CreateGameRequest request
    ) {

        Game game = new Game(
                request.name(),
                request.description(),
                request.price(),
                request.genre(),
                true
        );

        Game savedGame =
                gameRepository.save(game);

        return toResponse(savedGame);
    }

    public GameResponse update(
            long id,
            UpdateGameRequest request
    ) {

        Game game = gameRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new GameNotFoundException(id)
                );

        game.update(
                request.name(),
                request.description(),
                request.price(),
                request.genre(),
                request.available()
        );

        Game savedGame =
                gameRepository.save(game);

        return toResponse(savedGame);
    }

    public void delete(long id) {

        Game game = gameRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new GameNotFoundException(id)
                );

        gameRepository.delete(game);

        if(purchaseRepository.existsByGameId(id)) {
            throw new GameHasPurchasesException(id);
        }
    }

    private GameResponse toResponse(
            Game game
    ) {

        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getPrice(),
                game.getGenre(),
                game.isAvailable()
        );
    }
}