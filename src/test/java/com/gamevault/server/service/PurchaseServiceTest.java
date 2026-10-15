package com.gamevault.server.service;

import com.gamevault.server.dto.CreatePurchaseRequest;
import com.gamevault.server.exception.UserNotFoundException;
import com.gamevault.server.repository.GameRepository;
import com.gamevault.server.repository.PurchaseRepository;
import com.gamevault.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks private PurchaseService purchaseService;

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> purchaseService.purchaseGame(new CreatePurchaseRequest(1L), "ghost"));

        assertThrows(UserNotFoundException.class,
                () -> purchaseService.getUserLibrary("ghost"));
    }
}
