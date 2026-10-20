package com.gamevault.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.server.factory.TestDataFactory;
import com.gamevault.server.model.Game;
import com.gamevault.server.model.Genre;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseApiTest extends ApiTestBase{
    @Test
    void shouldPurchaseGameSuccessfully() throws Exception {
        long gameId = createAvailableGame();

        HttpResponse<String> response = purchaseGame(gameId, userToken);

        assertEquals(
                201,
                response.statusCode(),
                "Purchase failed: "  + response.body()
        );

        JsonNode purchase = objectMapper.readTree(response.body());

        assertEquals(
                gameId,
                purchase.get("gameId").asLong()
        );

        assertTrue(
                purchase.get("gameName").asText().startsWith(TestDataFactory.GAME_NAME_PREFIX),
                "Unexpected game name: " + purchase.get("gameName").asText()
        );

        assertEquals(
                0,
                purchase.get("pricePaid").decimalValue().compareTo(new java.math.BigDecimal("9.99"))
        );

        assertTrue(purchase.hasNonNull("id"));
        assertTrue(purchase.hasNonNull("purchasedAt"));
    }

    @Test
    void shouldRejectDuplicatePurchase()  throws Exception {
        long gameId = createAvailableGame();

        HttpResponse<String> firstPurchase = purchaseGame(gameId, userToken);

        assertEquals(
                201,
                firstPurchase.statusCode()
        );

        HttpResponse<String> secondPurchase = purchaseGame(gameId, userToken);

        assertEquals(
                409,
                secondPurchase.statusCode(),
                "Duplicate purchase should return 409 Conflict"
        );
    }

    @Test
    void shouldRejectPurchaseWithoutToken()  throws Exception {
        long gameId = createAvailableGame();

        HttpResponse<String> response = purchaseGame(gameId, null);

        assertTrue(
                response.statusCode() == 401 || response.statusCode() == 403,
                "Unauthenticated purchase must be rejected"
        );
    }

    @Test
    void shouldRejectPurchaseOfMissingGame()  throws Exception {
        HttpResponse<String> response = purchaseGame(
                9999999999L,
                userToken
        );

        assertEquals(
                404,
                response.statusCode(),
                "Purchase of game should return 404"
        );
    }

    @Test
    void shouldShowPurchasedGameOnlyInOwnersLibrary()  throws Exception {
        long gameId = createAvailableGame();

        HttpResponse<String> purchaseResponse = purchaseGame(gameId, userToken);

        assertEquals(
                201,
                purchaseResponse.statusCode(),
                "Purchase should succeed before checking the library"
        );

        //Getting User Library
        HttpResponse<String> ownerLibraryResponse = send(
                "GET",
                "/api/library",
                null,
                userToken
        );

        assertEquals(
                200,
                ownerLibraryResponse.statusCode()
        );

        JsonNode ownerLibrary = objectMapper.readTree(ownerLibraryResponse.body());

        boolean gameFound = false;

        for(JsonNode game : ownerLibrary) {
            if(game.get("gameId").asLong() == gameId) {
                gameFound = true;

                assertEquals(
                        0,
                        game.get("pricePaid").decimalValue().compareTo(new java.math.BigDecimal("9.99"))
                );
            }
        }

        assertTrue(
                gameFound,
                "Purchased game should appear in the owner's library"
        );

        //Second User Registration
        String otherUserToken = registerAndGetToken();

        //Checking library of second user
        HttpResponse<String> otherLibraryResponse = send(
                "GET",
                "/api/library",
                null,
                otherUserToken
        );

        assertEquals(
                200,
                otherLibraryResponse.statusCode()
        );

        JsonNode otherLibrary = objectMapper.readTree(otherLibraryResponse.body());

        for(JsonNode game : otherLibrary) {
            assertNotEquals(
                    gameId,
                    game.get("gameId").asLong(),
                    "Another user must not see someone else's purchased game"
            );
        }
    }

    @Test
    void shouldRejectPurchaseOfUnavailableGame()  throws Exception {
        Game game = gameRepository.save(
                new Game(
                        "Unavailable Game " + UUID.randomUUID(),
                        "This game cannot be purchased",
                        new java.math.BigDecimal("9.99"),
                        Genre.RPG,
                        false
                )
        );

        HttpResponse<String> response = purchaseGame(game.getId(), userToken);

        assertEquals(
                400,
                response.statusCode(),
                "Unavailable game should not be purchased"
        );
    }

    @Test
    void shouldRejectPurchaseWithoutGameId()  throws Exception {
        HttpResponse<String> response = send(
                "POST",
                "/api/purchases",
                "{}",
                userToken
        );

        assertEquals(
                400,
                response.statusCode(),
                "Purchase request without game id should return 400"
        );
    }

    @Test
    void shouldPreserveOriginalPurchasePrice()  throws Exception {
        long gameId = createAvailableGame();

        //Buy game for 9.99
        HttpResponse<String> purchaseResponse = purchaseGame(gameId, userToken);

        assertEquals(
                201,
                purchaseResponse.statusCode(),
                purchaseResponse.body()
        );

        JsonNode purchase = objectMapper.readTree(purchaseResponse.body());

        assertEquals(
                0,
                purchase.get("pricePaid").decimalValue().compareTo(new java.math.BigDecimal("9.99"))
        );

        //Change game price in test DB
        Game game = gameRepository.findById(gameId).orElseThrow();

        game.update(
                game.getName(),
                game.getDescription(),
                new java.math.BigDecimal("29.99"),
                game.getGenre(),
                game.isAvailable()
        );

        gameRepository.save(game);

        //Checking user library
        HttpResponse<String> libraryResponse = send(
                "GET",
                "/api/library",
                null,
                userToken
        );

        assertEquals(
                200,
                libraryResponse.statusCode()
        );

        JsonNode library = objectMapper.readTree(libraryResponse.body());

        boolean gameFound = false;

        for(JsonNode libraryGame : library) {
            if(libraryGame.get("gameId").asLong() == gameId) {
                gameFound = true;

                assertEquals(
                        0,
                        libraryGame.get("pricePaid").decimalValue().compareTo(new java.math.BigDecimal("9.99")),
                        "Library should preserve the original purchase price"
                );
            }
        }

        assertTrue(
                gameFound,
                "Purchased game should remain in the library"
        );
    }

    @Test
    void shouldAllowDifferentUsersToPurchaseSameGame()  throws Exception {
        long gameId = createAvailableGame();

        //First user buy game
        HttpResponse<String> firstPurchase = purchaseGame(gameId, userToken);

        assertEquals(
                201,
                firstPurchase.statusCode()
        );

        //Second user registration
        String secondUserToken = registerAndGetToken();

        //Second user buy the same game
        HttpResponse<String> secondPurchase = purchaseGame(gameId, secondUserToken);

        assertEquals(
                201,
                secondPurchase.statusCode(),
                "A different user should be able to purchase the same game"
        );

        JsonNode purchase = objectMapper.readTree(secondPurchase.body());

        assertEquals(
                gameId,
                purchase.get("gameId").asLong()
        );
    }

    @Test
    void shouldReturnEmptyLibraryForNewUser()  throws Exception {
        String token = registerAndGetToken();

        HttpResponse<String> response = send(
                "GET",
                "/api/library",
                null,
                token
        );

        assertEquals(
                200,
                response.statusCode(),
                response.body()
        );

        JsonNode library = objectMapper.readTree(response.body());
        assertTrue(library.isArray());
        assertEquals(
                0,
                library.size()
        );
    }

    @Test
    void shouldRejectLibraryWithoutToken() throws  Exception {
        int status = send(
                "GET",
                "/api/library",
                null,
                null
        ).statusCode();

        assertTrue(
                status == 401 || status == 403,
                "Status was " + status
        );
    }

    @Test
    void shouldNotDeleteGameThatWasPurchased()  throws Exception {
        long gameId = createAvailableGame();
        assertEquals(
                201,
                purchaseGame(gameId, userToken).statusCode()
        );

        HttpResponse<String> response = send(
                "DELETE",
                "/api/games/" + gameId,
                null,
                adminToken
        );

        assertEquals(
                409,
                response.statusCode(),
                response.body()
        );
    }
}
