package com.gamevault.server.config;

import com.gamevault.server.model.User;
import com.gamevault.server.model.UserRole;
import com.gamevault.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.username:admin}") String username,
            @Value("${app.bootstrap.admin.email:admin@example.com}") String email,
            @Value("${app.bootstrap.admin.password:}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if(password.isBlank()) {
            log.info("Admin bootstrap skipped: app.bootstrap.admin.password is not set");
            return;
        }

        if(userRepository.countByRole(UserRole.ADMIN) > 0) {
            log.info("Admin bootstrap skipped: an admin already exists");
            return;
        }

        if(password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("app.bootstrap.admin.password must be at least "
                    + MIN_PASSWORD_LENGTH + " characters long");
        }

        if(userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            log.warn("Admin bootstrap skipped: username '{}' or email '{}' is already taken "
                    + "by a non-admin user", username, email);
            return;
        }

        userRepository.save(new User(
                username,
                email,
                passwordEncoder.encode(password),
                UserRole.ADMIN
        ));
        log.info("Admin bootstrap: created admin user '{}'", username);
    }
}
