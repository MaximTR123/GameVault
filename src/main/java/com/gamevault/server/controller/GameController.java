package com.gamevault.server.controller;

import com.gamevault.server.dto.CreateGameRequest;
import com.gamevault.server.dto.GameResponse;
import com.gamevault.server.dto.UpdateGameRequest;
import com.gamevault.server.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<GameResponse> getAllGames() {

        return gameService.getAll();
    }

    @GetMapping("/{id}")
    public GameResponse getGameById(
            @PathVariable long id
    ) {

        return gameService.getById(id);
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(
            @Valid
            @RequestBody
            CreateGameRequest request
    ) {

        GameResponse game =
                gameService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(game);
    }

    @PutMapping("/{id}")
    public GameResponse updateGame(
            @PathVariable long id,

            @Valid
            @RequestBody
            UpdateGameRequest request
    ) {

        return gameService.update(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(
            @PathVariable long id
    ) {

        gameService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}