package com.gamevault.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameApiTest {

    @Value("${local.server.port}")
    int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void shouldGetGamesSuccessfully() throws Exception {
        assertEquals(200, get("/api/games").statusCode());
    }

    @Test
    void shouldReturn404ForMissingGame() throws Exception {
        assertEquals(404, get("/api/games/999999").statusCode());
    }

    @Test
    void shouldGetGamesSuccsessfully() throws Exception {
        HttpResponse<String> response = get("/api/games");

        assertEquals(200, response.statusCode());

        assertTrue(
                response.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .contains("application/json")
        );

        assertTrue(response.body().startsWith("["));
    }
}