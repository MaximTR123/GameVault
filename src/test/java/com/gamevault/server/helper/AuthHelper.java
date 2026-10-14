package com.gamevault.server.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.server.client.ApiClient;
import com.gamevault.server.dto.LoginRequest;

import java.io.IOException;
import java.util.Map;

public class AuthHelper {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public AuthHelper(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }
    public String loginAndGetToken(String username, String password)
            throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", username,
                        "password", password
                )
        );

        var response = apiClient.send(
                "POST",
                "/api/auth/login",
                body,
                null
        );

        if(response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Login failed. HTTP status: " + response.statusCode()
                    + ", response: " +  response.body()
            );
        }

        JsonNode json = objectMapper.readTree(response.body());

        return json.get("token").asText();
    }
}
