package com.gamevault.server.dto;

import com.gamevault.server.model.UserRole;

public record LoginResponse(
        Long id,
        String username,
        String email,
        UserRole role
) {
}
