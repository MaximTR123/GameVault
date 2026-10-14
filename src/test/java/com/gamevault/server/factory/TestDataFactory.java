package com.gamevault.server.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.server.client.ApiClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class TestDataFactory {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final String adminToken;
    public static final String GAME_NAME_PREFIX = "Test Game ";

    public TestDataFactory(
            ApiClient apiClient,
            ObjectMapper objectMapper,
            String adminToken
    ) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
        this.adminToken = adminToken;
    }

    public long createAvailableGame() throws IOException, InterruptedException {
        String uniqueName = GAME_NAME_PREFIX + UUID.randomUUID();

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "name", uniqueName,
                        "description", "Game created for automated testing",
                        "price", new BigDecimal("9.99"),
                        "genre", "RPG",
                        "available", true
                )
        );

        var response = apiClient.send(
                "POST",
                "/api/games",
                body,
                adminToken
        );

        if(response.statusCode() != 201) {
            throw new IllegalStateException(
                    "Could not create test game. HTTP status: "
                    + response.statusCode()
                    + ", response: " + response.body()
            );
        }

        return objectMapper.readTree(response.body()).get("id").asLong();
    }
}
