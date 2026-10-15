package com.gamevault.server.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.server.client.ApiClient;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class UserTestHelper {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public UserTestHelper(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    public String registerAndGetToken() throws IOException, InterruptedException {
        String uniqueId = UUID.randomUUID().toString();

        String username = "testuser_" + uniqueId;
        String email = username + "@example.com";
        String password = "TestPassword123";

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", username,
                        "email", email,
                        "password", password
                )
        );

        var response = apiClient.send(
                "POST",
                "/api/auth/register",
                body,
                null
        );

        if(response.statusCode() != 201) {
            throw new IllegalStateException(
                    "Registration failed. HTTP status: "
                    + response.statusCode()
                    + ", response: " + response.body()
            );
        }

        return new AuthHelper(apiClient, objectMapper).loginAndGetToken(username, password);
    }
}
