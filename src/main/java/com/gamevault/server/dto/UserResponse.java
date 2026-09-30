package com.gamevault.server.dto;

import com.gamevault.server.model.UserRole;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        LocalDateTime createdAt
) {
}
