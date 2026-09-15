package com.gamevault.server.service;

import com.gamevault.server.dto.CreateGameRequest;
import com.gamevault.server.dto.GameResponse;
import com.gamevault.server.dto.UpdateGameRequest;
import com.gamevault.server.exception.GameNotFoundException;
import com.gamevault.server.model.Game;
import com.gamevault.server.model.Genre;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class GameService {

    private final Map<Long, Game> games =
            new LinkedHashMap<>();

    private final AtomicLong idSequence =
            new AtomicLong(0);

    public GameService() {

        create(new CreateGameRequest(
                "Cyber Samurai",
                "Cyberpunk action game",
                new BigDecimal("79.99"),
                Genre.ACTION
        ));

        create(new CreateGameRequest(
                "Dead Sector",
                "Survival horror game",
                new BigDecimal("129.99"),
                Genre.HORROR
        ));

        create(new CreateGameRequest(
                "Dungeon Protocol",
                "Dark fantasy RPG",
                new BigDecimal("59.99"),
                Genre.RPG
        ));
    }

    public List<GameResponse> getAll() {

        List<GameResponse> result =
                new ArrayList<>();

        for (Game game : games.values()) {
            result.add(toResponse(game));
        }

        return result;
    }

    public GameResponse getById(long id) {

        Game game = games.get(id);

        if (game == null) {
            throw new GameNotFoundException(id);
        }

        return toResponse(game);
    }

    public GameResponse create(
            CreateGameRequest request
    ) {

        long id = idSequence.incrementAndGet();

        Game game = new Game(
                id,
                request.name(),
                request.description(),
                request.price(),
                request.genre(),
                true
        );

        games.put(id, game);

        return toResponse(game);
    }

    public GameResponse update(
            long id,
            UpdateGameRequest request
    ) {

        if (!games.containsKey(id)) {
            throw new GameNotFoundException(id);
        }

        Game updatedGame = new Game(
                id,
                request.name(),
                request.description(),
                request.price(),
                request.genre(),
                request.available()
        );

        games.put(id, updatedGame);

        return toResponse(updatedGame);
    }

    public void delete(long id) {

        if (!games.containsKey(id)) {
            throw new GameNotFoundException(id);
        }

        games.remove(id);
    }

    private GameResponse toResponse(Game game) {

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