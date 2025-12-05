package org.example.qposbackend.Authorization.User.userShop.dto;

import lombok.Data;
import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.shop.Shop;

import java.util.Date;

@Data
public class UserShopResponse {
    private Long id;
    private User user;
    private Shop shop;
    private SystemRole role;
    private boolean isDefault;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;
} 