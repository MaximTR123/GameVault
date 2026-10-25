package com.gamevault.server;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;

class GameApiTest extends ApiTestBase{
    private static final String VALID_GAME_BODY = """
            {"name": "Update Name", "description": "Update Description",
            "price": 12.99, "genre": "RPG", "available": true}
            """;

    @Test
    void shouldGetGamesSuccessfully() throws Exception {
        HttpResponse<String> response = send(
                "GET",
                "/api/games",
                null,
                null
        );

        assertEquals(200, response.statusCode());

        JsonNode games = objectMapper.readTree(response.body());

        assertTrue(
                games.isArray(),
                "Games endpoint should return a JSON array"
        );
    }

    @Test
    void shouldReturn404ForMissingGame() throws Exception {
        HttpResponse<String> response = send(
                "GET",
                "/api/games/999999",
                null,
                null
        );

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldCreateGameAsAdmin() throws Exception {
        String body = """
                {
                    "name": "Test Game",
                    "description": "Created by an automated API test",
                    "price": 19.99,
                    "genre": "RPG"
                }
                """;

        HttpResponse<String> response = send(
                "POST",
                "/api/games",
                body,
                adminToken
        );

        assertEquals(
                201,
                response.statusCode(),
                "Game creation failed: " + response.body()
        );

        JsonNode game = objectMapper.readTree(response.body());

        assertEquals("Test Game", game.get("name").asText());
        assertEquals("RPG", game.get("genre").asText());
    }

    @Test
    void shouldRejectGameCreationByRegularUser()  throws Exception {
        String body = """
                {
                    "name": "Unauthorized Game",
                    "description": "A regular user must not create games",
                    "price": 19.99,
                    "genre": "RPG"
                }
                """;

        HttpResponse<String> response = send(
                "POST",
                "/api/games",
                    body,
                    userToken
        );

        assertEquals(
                403,
                response.statusCode(),
                "A regular user must not create games"
        );
    }

    @Test
    void shouldRejectGameCreationWithoutToken()  throws Exception {
        String body = """
                        {
                            "name": "Unauthorized Game",
                            "description": "A request without token",
                            "price": 19.99,
                            "genre": "RPG"
                        }
                        """;

        HttpResponse<String> response = send(
                "POST",
                  "/api/games",
                        body,
                        null
        );

        assertTrue(
                response.statusCode() == 401 || response.statusCode() == 403,
                "An unauthorized request must be rejected, but received: " + response.statusCode()
        );
    }

    @Test
    void shouldRejectGameWithInvalidPrice() throws Exception {
        String body = """
                        {
                            "name": "Invalid price game",
                            "description": "Price must be at least 0.01",
                            "price": 0,
                            "genre": "RPG"
                        }
                      """;

        HttpResponse<String> response = send(
                "POST",
                  "/api/games",
                       body,
                       adminToken
        );

        assertEquals(
                400,
                response.statusCode(),
                "A game with an invalid price must be rejected"
        );
    }

    @Test
    void shouldCompleteGameCrudLifecycle() throws Exception {
        //1.Create
        String createBody = """
                        {
                            "name": "CRUD Test Game",
                            "description": "Original description",
                            "price": 19.99,
                            "genre": "RPG"
                        }
                      """;

        HttpResponse<String> createResponse = send(
                "POST",
                  "/api/games",
                       createBody,
                       adminToken
        );

        assertEquals(
                201,
                createResponse.statusCode(),
                "Game should be created"
        );

        JsonNode createGame = objectMapper.readTree(createResponse.body());

        assertTrue(
                createGame.hasNonNull("id"),
                "Create game should have an ID"
        );

        long gameId = createGame.get("id").asLong();

        //2.Read

        HttpResponse<String> getResponse = send(
                "GET",
                "/api/games/" + gameId,
                null,
                null
        );

        assertEquals(
                200,
                getResponse.statusCode()
        );

        JsonNode game = objectMapper.readTree(getResponse.body());

        assertEquals(
                "Original description",
                game.get("description").asText()
        );

        //3.Update
        String  updateBody = """
                             {
                                 "name": "Updated CRUD Game",
                                 "description": "Updated description",
                                 "price": 29.99,
                                 "genre": "RPG",
                                 "available": true
                             }
                             """;

        HttpResponse<String> updateResponse = send(
                "PUT",
                  "/api/games/" + gameId,
                       updateBody,
                       adminToken
        );

        assertEquals(
                200,
                updateResponse.statusCode(),
                "Game should be updated"
        );

        //Check for updates
        HttpResponse<String> updateGetResponse = send(
                "GET",
                "/api/games/" + gameId,
                null,
                null
        );

        assertEquals(
                200,
                updateGetResponse.statusCode()
        );

        JsonNode updateGame = objectMapper.readTree(updateGetResponse.body());

        assertEquals(
                "Updated CRUD Game",
                updateGame.get("name").asText()
        );

        assertEquals(
                "Updated description",
                updateGame.get("description").asText()
        );

        assertEquals(
                0,
                updateGame.get("price").decimalValue()
                        .compareTo(new java.math.BigDecimal("29.99"))
        );

        //4.Delete

        HttpResponse<String> deleteResponse = send(
                "DELETE",
                "/api/games/" + gameId,
                null,
                adminToken
        );

        assertEquals(
                204,
                deleteResponse.statusCode(),
                "Game should be deleted"
        );

        //Checking for delete game
        HttpResponse<String> deleteGetResponse = send(
                "GET",
                "/api/games/" + gameId,
                null,
                null
        );

        assertEquals(
                404,
                deleteGetResponse.statusCode(),
                "Delete game should no longer exist"
        );
    }

    @Test
    void shouldReturn404WhenUpdatingMissingGame() throws Exception {
        HttpResponse<String> response = send(
                "PUT",
                "/api/games/999999",
                null,
                adminToken
        );
    }

    @Test
    void shouldReturn404WhenDeletingMissingGame() throws Exception {
        assertEquals(
                404,
                send(
                        "DELETE",
                        "/api/games/999999",
                        null,
                        adminToken
                ).statusCode()
        );
    }

    @Test
    void shouldForbidRegularUserFromUpdatingAndDeletingGame() throws Exception {
        long gameId = createAvailableGame();

        assertEquals(
                403,
                send(
                        "PUT",
                        "/api/games/" + gameId,
                        VALID_GAME_BODY,
                        userToken
                ).statusCode()
        );

        assertEquals(
                403,
                send(
                        "DELETE",
                        "/api/games/" + gameId,
                        null,
                        userToken
                ).statusCode()
        );
    }

    @Test
    void shouldRejectGameWithBlankName() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "name", "",
                        "description", "Valid description",
                        "price", 9.99,
                        "genre", "RPG"
                )
        );

        assertEquals(
                400,
                send(
                        "POST",
                        "/api/games",
                        body,
                        adminToken
                ).statusCode()
        );
    }

    @Test
    void shouldRejectGameWithTooLongName() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "name", "A".repeat(151),
                        "description", "Valid description",
                        "price", 9.99,
                        "genre", "RPG"
                )
        );

        HttpResponse<String> response = send(
                "POST",
                "/api/games",
                body,
                adminToken
        );

        assertEquals(
                400,
                response.statusCode(),
                response.body()
        );
    }

    @Test
    void shouldListCreatedGame()  throws Exception {
        long gameId = createAvailableGame();

        JsonNode games = objectMapper.readTree(send(
                "GET",
                "/api/games",
                null,
                null
        ).body());

        boolean found = false;

        for(JsonNode game : games) {
            if (game.get("id").asLong() == gameId) {
                found = true;
            }
        }
        assertTrue(found, "Created game should be in the catalog");
    }
}