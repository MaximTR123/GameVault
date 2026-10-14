
package com.gamevault.server;

import com.gamevault.server.model.Game;
import com.gamevault.server.model.Genre;
import com.gamevault.server.repository.GameRepository;
import com.gamevault.server.client.ApiClient;
import com.gamevault.server.model.User;
import com.gamevault.server.model.UserRole;
import com.gamevault.server.repository.UserRepository;
import com.gamevault.server.helper.AuthHelper;
import com.gamevault.server.factory.TestDataFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameApiTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    private static final String ADMIN_USERNAME = "testadmin";
    private static final String ADMIN_PASSWORD = "TestPassword123";
    private static final String ADMIN_EMAIL = "testadmin@gamevault.test";

    private String userToken;

    private static final String USER_USERNAME = "testuser";
    private static final String USER_PASSWORD = "TestPassword123";
    private static final String USER_EMAIL = "testuser@example.com";

    private ApiClient apiClient;
    private AuthHelper authHelper;
    private TestDataFactory testDataFactory;

    private long createAvailableGame() throws IOException, InterruptedException {
        return testDataFactory.createAvailableGame();
    }

    private HttpResponse<String> purchaseGame(long gameId, String token) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "gameId", gameId
                )
        );

        return send(
                "POST",
                "/api/purchases",
                body,
                token
        );
    }

    private String registerAndGetToken() throws Exception {
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        String username = "library_" + uniqueId;
        String password = "SecurePassword123";
        String email = username + "@gmail.com";

        String body = objectMapper.writeValueAsString(
                Map.of(
                    "username", username,
                        "email", email,
                        "password", password
                )
        );

        HttpResponse<String> registerResponse = send(
                "POST",
                "/api/auth/register",
                body,
                null
        );

        assertEquals(
                201,
                registerResponse.statusCode(),
                "Test user registration failed: " + registerResponse.body()
        );

        return authHelper.loginAndGetToken(username, password);
    }

    @BeforeEach
    void setUp() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port);
        authHelper = new AuthHelper(apiClient, objectMapper);

        userRepository.findByUsername(ADMIN_USERNAME)
                .orElseGet(() -> userRepository.save(
                        new User(
                                ADMIN_USERNAME,
                                ADMIN_EMAIL,
                                passwordEncoder.encode(ADMIN_PASSWORD),
                                UserRole.ADMIN
                        )
                ));

        adminToken = authHelper.loginAndGetToken(
                ADMIN_USERNAME,
                ADMIN_PASSWORD
        );

        testDataFactory = new TestDataFactory(
                apiClient,
                objectMapper,
                adminToken
        );

        userRepository.findByUsername(USER_USERNAME)
                .orElseGet(() -> userRepository.save(
                        new User(
                                USER_USERNAME,
                                USER_EMAIL,
                                passwordEncoder.encode(USER_PASSWORD),
                                UserRole.USER
                        )
                ));

        userToken = authHelper.loginAndGetToken(
                USER_USERNAME,
                USER_PASSWORD
        );
    }

    private HttpResponse<String> send(
            String method,
            String path,
            String body,
            String token
    ) throws Exception {
        return apiClient.send(method, path, body, token);
    }

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
    void shouldRegisterUserSuccessfully() throws Exception {
        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        String username = "newuser_" + uniqueId;
        String password = "SecurePassword123";
        String email = "newuser_" + uniqueId + "@test.com";

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", username,
                        "email", email,
                        "password", password
                )
        );

        HttpResponse<String> response = send(
                "POST",
                "/api/auth/register",
                body,
                null
        );

        assertEquals(
                201,
                response.statusCode(),
                "Registration should succeed: " + response.body()
        );

        JsonNode user = objectMapper.readTree(response.body());

        assertEquals(
                username,
                user.get("username").asText()
        );

        assertEquals(
                email,
                user.get("email").asText()
        );

        assertFalse(
                user.has("password"),
                "API must not expose the user's password"
        );
    }

    @Test
    void shouldRejectRegistrationWithExistingUsername() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", ADMIN_USERNAME,
                        "email", "different_" + UUID.randomUUID() + "@test.com",
                        "password", "SecurePassword123"
                )
        );

        HttpResponse<String> response = send(
                "POST",
                "/api/auth/register",
                body,
                null
        );

        assertEquals(
                409,
                response.statusCode(),
                "Duplicate username should return 409 conflict"
        );
    }

    @Test
    void shouldRejectRegistrationWithExistingEmail() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", "another_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                        "email", ADMIN_EMAIL,
                        "password", "SecurePassword123"
                )
        );

        HttpResponse<String> response = send(
                "POST",
                "/api/auth/register",
                body,
                null
        );

        assertEquals(
                409,
                response.statusCode(),
                "Duplicate email should return 409 conflict"
        );
    }

    @Test
    void shouldRejectLoginWithIncorrectPassword() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", ADMIN_USERNAME,
                        "password", "DefinitelyWrongPassword123"
                )
        );

        HttpResponse<String> response = send(
                "POST",
                "/api/auth/login",
                body,
                null
        );

        assertEquals(
                401,
                response.statusCode(),
                "Incorrect password should return 401 Unauthorized"
        );
    }

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
}