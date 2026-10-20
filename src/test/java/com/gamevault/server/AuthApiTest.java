package com.gamevault.server;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AuthApiTest extends ApiTestBase{
    @ParameterizedTest
    @CsvSource({
            "ab,valid@example.com,SecurePassword",
            "validuser,not-an-email,SecurePassword",
            "validuser,valid@example.com,short"
    })
    void shouldRejectInvalidRegistration(String username, String email, String password) throws Exception {
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
                400,
                response.statusCode(),
                response.body()
        );
    }

    @Test
    void shouldRejectLoginOfUnknownUser() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", "nobody_" + UUID.randomUUID(),
                        "password", "SecurePassword123"
                )
        );

        assertEquals(
                401,
                send(
                        "POST",
                        "/api/auth/login",
                        body,
                        null
                ).statusCode()
        );
    }

    @Test
    void shouldRejectLoginWithBlankPassword() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", ADMIN_USERNAME,
                        "password", ""
                )
        );

        assertEquals(
                400,
                send(
                        "POST",
                        "/api/auth/login",
                        body,
                        null
                ).statusCode()
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
}
