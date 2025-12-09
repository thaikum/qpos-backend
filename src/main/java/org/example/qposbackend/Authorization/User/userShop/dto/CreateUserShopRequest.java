package org.example.qposbackend.Authorization.User.userShop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.qposbackend.Authorization.User.dto.UserDto;

@Data
public class CreateUserShopRequest {
    private Long userId;
    private UserDto user;
    @NotNull(message = "Role ID is required")
    private String roleId;
    private boolean isDefault = false;
} 