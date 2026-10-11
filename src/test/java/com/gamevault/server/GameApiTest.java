
package com.gamevault.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.server.model.User;
import com.gamevault.server.model.UserRole;
import com.gamevault.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
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


    @BeforeEach
    void setUp() throws Exception {
        userRepository.findByUsername(ADMIN_USERNAME)
                .orElseGet(() -> userRepository.save(
                        new User(
                                ADMIN_USERNAME,
                                ADMIN_EMAIL,
                                passwordEncoder.encode(ADMIN_PASSWORD),
                                UserRole.ADMIN
                        )
                ));

        adminToken = loginAndGetToken(
                ADMIN_USERNAME,
                ADMIN_PASSWORD
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

        userToken = loginAndGetToken(
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

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE);

        if (body != null) {
            builder.header(
                    "Content-Type",
                    MediaType.APPLICATION_JSON_VALUE
            );
        }

        if (token != null) {
            builder.header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + token
            );
        }

        switch (method) {
            case "GET" -> builder.GET();
            case "POST" -> builder.POST(
                    HttpRequest.BodyPublishers.ofString(body)
            );
            case "PUT" -> builder.PUT(
                    HttpRequest.BodyPublishers.ofString(body)
            );
            case "DELETE" -> builder.DELETE();
            default -> throw new IllegalArgumentException(
                    "Unsupported HTTP method: " + method
            );
        }

        return client.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private String loginAndGetToken(
            String username,
            String password
    ) throws Exception {

        String body = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "username", username,
                        "password", password
                )
        );

        HttpResponse<String> response = send(
                "POST",
                "/api/auth/login",
                body,
                null
        );

        assertEquals(
                200,
                response.statusCode(),
                "Login failed: " + response.body()
        );

        JsonNode json = objectMapper.readTree(response.body());

        assertTrue(
                json.hasNonNull("token"),
                "Login response must contain a token"
        );

        return json.get("token").asText();
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
}