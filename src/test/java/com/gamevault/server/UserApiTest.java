package com.gamevault.server;

import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserApiTest extends ApiTestBase{
    private String uniqueUsername() {
        return "tmp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private JsonNode createUserAsAdmin(
            String username,
            String password,
            String role
    ) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", username,
                        "email", username + "@example.com",
                        "password", password,
                        "role", role
                )
        );

        HttpResponse<String> response = send(
                "POST",
                "/api/users",
                body,
                adminToken
        );

        assertEquals(
                201,
                response.statusCode(),
                response.body()
        );

        return objectMapper.readTree(response.body());
    }

    @Test
    void shouldReturn404WhenGettingMissingUser() throws Exception {
        HttpResponse<String> response = send(
                "GET",
                "/api/users/" + MISSING_USER_ID,
                null,
                adminToken
        );

        assertEquals(
                404,
                response.statusCode(),
                response.body()
        );

        assertEquals(
                "User not found", response.body()
        );
    }

    @Test
    void shouldReturn404WhenDeletingMissingUser() throws Exception {
        HttpResponse<String> response = send(
                "DELETE",
                "/api/users/" + MISSING_USER_ID,
                null,
                adminToken
        );

        assertEquals(
                404,
                response.statusCode(),
                response.body()
        );

        assertEquals(
                "User not found", response.body()
        );
    }

    @Test
    void shouldReturn404WhenUpdatingRoleOfMissingUser() throws Exception {
        HttpResponse<String> response = send(
                "PUT",
                "/api/users/" + MISSING_USER_ID +"/role",
                "{\"role\": \"USER\"}",
                adminToken
        );

        assertEquals(
                404,
                response.statusCode(),
                response.body()
        );

        assertEquals(
                "User not found", response.body()
        );
    }

    @Test
    void shouldRejectTokenOfDeletedUser() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String username = "deleted_" + suffix;
        String password = "SecurePassword123";

        String registerBody = objectMapper.writeValueAsString(
                Map.of(
                        "username", username,
                        "email", username + "@example.com",
                        "password", password
                )
        );

        HttpResponse<String> registerResponse = send(
                "POST",
                "/api/auth/register",
                registerBody,
                null
        );

        assertEquals(
                201,
                registerResponse.statusCode(),
                registerResponse.body()
        );

        long userId = objectMapper.readTree(registerResponse.body()).get("id").asLong();
        String token = authHelper.loginAndGetToken(username, password);

        assertEquals(
                204,
                send("DELETE", "/api/users/" + userId, null, adminToken).statusCode()
        );

        int status = send("GET", "/api/library", null, token).statusCode();
        assertTrue(
                status == 401 || status == 403,
                "Token of a deleted user must be rejected, but status was " + status
        );
    }

    @Test
    void shouldRejectGarbageToken() throws Exception {
        int status = send(
                "GET",
                "/api/library",
                null,
                "abc.def.ghi"
        ).statusCode();

        assertTrue(
                status == 401 || status == 403,
                "Status was " + status
        );
    }

    @Test
    void shouldRejectTokenSignedWithWrongKet() throws Exception {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "another-secret-key-that-is-long-enough-0123456789"
                        .getBytes(StandardCharsets.UTF_8)
        );

        String forgedToken = Jwts.builder()
                .subject(ADMIN_USERNAME)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(wrongKey)
                .compact();

        int status = send(
                "GET",
                "/api/users",
                null,
                forgedToken
        ).statusCode();

        assertTrue(
                status == 401 || status == 403,
                "Status was " + status
        );
    }

    @Test
    void shouldForbidRegularUserFromAdminEndpoints() throws Exception {
        assertEquals(
                403,
                send(
                        "GET",
                        "/api/users",
                        null,
                        userToken
                ).statusCode()
        );

        assertEquals(
                403,
                send(
                        "GET",
                        "/api/users/1",
                        null,
                        userToken
                ).statusCode()
        );

        assertEquals(
                403,
                send(
                        "DELETE",
                        "/api/users/1",
                        null,
                        userToken
                ).statusCode()
        );

        assertEquals(
                403,
                send(
                        "PUT",
                        "/api/users/1/role",
                        "{\"role\": \"ADMIN\"}",
                        userToken
                ).statusCode()
        );
    }

    @Test
    void shouldListUsersAsAdminWithoutPasswords() throws Exception {
        HttpResponse<String> response = send(
                "GET",
                "/api/users",
                null,
                adminToken
        );

        assertEquals(
                200,
                response.statusCode(),
                response.body()
        );

        JsonNode users = objectMapper.readTree(response.body());
        assertTrue(users.isArray());
        assertTrue(users.size() >= 2);

        for (JsonNode user : users) {
            assertFalse(user.has("password"), "API must not expose passwords");
        }
    }

    @Test
    void shouldCreateUserAsAdmin() throws Exception {
        String username = uniqueUsername();

        JsonNode created = createUserAsAdmin(username, "SecurePassword123", "USER");

        assertEquals(username, created.get("username").asText());
        assertEquals("USER", created.get("role").asText());
        assertFalse(created.has("password"));
        assertNotNull(authHelper.loginAndGetToken(username, "SecurePassword123"));
    }

    @Test
    void shouldNotAllowAdminToDeleteSelf() throws Exception {
        long adminId = userRepository.findByUsername(ADMIN_USERNAME).orElseThrow().getId();

        HttpResponse<String> response = send(
                "DELETE",
                "/api/users/" + adminId,
                null,
                adminToken
        );

        assertEquals(
                400,
                response.statusCode(),
                response.body()
        );
    }

    @Test
    void shouldNotAllowDemotingLastAdmin() throws Exception {
        long adminId = userRepository.findByUsername(ADMIN_USERNAME).orElseThrow().getId();

        HttpResponse<String> response = send(
                "PUT",
                "/api/users/" + adminId + "/role",
                "{\"role\": \"USER\"}",
                adminToken
        );

        assertEquals(
                400,
                response.statusCode(),
                response.body()
        );
    }

    @Test
    void shouldRejectLoginOfDeletedUser() throws Exception {
        String username = uniqueUsername();
        long id = createUserAsAdmin(username, "SecurePassword123", "USER").get("id").asLong();

        assertEquals(
                204,
                send(
                        "DELETE",
                        "/api/users/" + id,
                        null,
                        adminToken
                ).statusCode()
        );

        String loginBody = objectMapper.writeValueAsString(
                Map.of(
                        "username", username,
                        "password", "SecurePassword123"
                )
        );

        assertEquals(
                401,
                send(
                        "POST",
                        "/api/auth/login",
                        loginBody,
                        null
                ).statusCode()
        );
    }

    @Test
    void shouldPromoteUserToAdmin() throws Exception {
        long id = createUserAsAdmin(uniqueUsername(), "SecurePassword123", "USER").get("id").asLong();

        try {
            HttpResponse<String> response = send(
                    "PUT",
                    "/api/users/" + id + "/role",
                    "{\"role\": \"ADMIN\"}",
                    adminToken
            );

            assertEquals(
                    200,
                    response.statusCode(),
                    response.body()
            );

            assertEquals(
                    "ADMIN",
                    objectMapper.readTree(response.body()).get("role").asText()
            );
        } finally {
            send(
                    "DELETE",
                    "/api/users/" + id,
                    null,
                    adminToken
            );
        }
    }
}