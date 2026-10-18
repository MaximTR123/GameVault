package com.gamevault.server.config;

import com.gamevault.server.model.User;
import com.gamevault.server.model.UserRole;
import com.gamevault.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {
    private static final String USERNAME = "admin";
    private static final String EMAIL = "admin@example.com";
    private static final String PASSWORD = "StrongPassword123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminBootstrap bootstrap(String password) {
        return new AdminBootstrap(userRepository, passwordEncoder, USERNAME, EMAIL, password);
    }

    @Test
    void shouldDoNothingWhenPasswordIsNotConfigured() {
        bootstrap("").run(null);

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldDoNothingWhenAdminAlreadyExists() {
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        bootstrap(PASSWORD).run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectTooShortPassword() {
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(0L);

        assertThrows(IllegalStateException.class, () -> bootstrap("short").run(null));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldSkipWhenUsernameIsTaken() {
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(0L);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
    }

    @Test
    void shouldCreateAdminWithEncodedPassword() {
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(0L);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-password");

        bootstrap(PASSWORD).run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(UserRole.ADMIN, saved.getRole());
        assertEquals("encoded-password", saved.getPassword());
    }
}
