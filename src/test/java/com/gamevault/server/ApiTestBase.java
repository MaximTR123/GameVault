package com.gamevault.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.server.client.ApiClient;
import com.gamevault.server.factory.TestDataFactory;
import com.gamevault.server.helper.AuthHelper;
import com.gamevault.server.helper.UserTestHelper;
import com.gamevault.server.model.User;
import com.gamevault.server.model.UserRole;
import com.gamevault.server.repository.GameRepository;
import com.gamevault.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class ApiTestBase {
    protected static final String ADMIN_USERNAME = "testadmin";
    protected static final String ADMIN_PASSWORD = "TestPassword123";
    protected static final String ADMIN_EMAIL = "testadmin@gamevault.test";

    protected static final String USER_USERNAME = "testuser";
    protected static final String USER_PASSWORD = "TestPassword123";
    protected static final String USER_EMAIL = "testuser@example.com";

    protected static final long MISSING_USER_ID = 999_999L;

    @Value("${local.server.port}")
    protected int port;

    @Autowired
    protected GameRepository gameRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected ApiClient apiClient;
    protected AuthHelper authHelper;
    protected TestDataFactory testDataFactory;
    protected UserTestHelper userTestHelper;

    protected String adminToken;
    protected String userToken;

    @BeforeEach
    void setUpBase() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port);
        authHelper = new AuthHelper(apiClient, objectMapper);
        userTestHelper = new UserTestHelper(apiClient, objectMapper);

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

    protected HttpResponse<String> send(
            String method,
            String path,
            String body,
            String token
    ) throws Exception {
        return apiClient.send(method, path, body, token);
    }

    protected long createAvailableGame() throws IOException, InterruptedException {
        return testDataFactory.createAvailableGame();
    }

    protected HttpResponse<String> purchaseGame(long gameId, String token) throws Exception {
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

    protected String registerAndGetToken() throws IOException, InterruptedException {
        return userTestHelper.registerAndGetToken();
    }
}
