package com.gamevault.server.dto;

import com.gamevault.server.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
}
