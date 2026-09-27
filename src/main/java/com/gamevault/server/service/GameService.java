package com.gamevault.server.service;

import com.gamevault.server.dto.CreateGameRequest;
import com.gamevault.server.dto.GameResponse;
import com.gamevault.server.dto.UpdateGameRequest;
import com.gamevault.server.exception.GameNotFoundException;
import com.gamevault.server.model.Game;
import com.gamevault.server.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(
            GameRepository gameRepository
    ) {

        this.gameRepository = gameRepository;
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