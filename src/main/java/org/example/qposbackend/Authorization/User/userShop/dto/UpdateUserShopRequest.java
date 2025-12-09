package org.example.qposbackend.Authorization.User.userShop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserShopRequest {
    @NotNull(message = "UserShop ID is required")
    private Long id;
    
    private String roleId;
    private Boolean isDefault;
    private Boolean isActive;
} 