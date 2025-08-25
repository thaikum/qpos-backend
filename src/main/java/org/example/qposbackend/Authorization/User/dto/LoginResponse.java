package org.example.qposbackend.Authorization.User.dto;

import org.example.qposbackend.Authorization.User.userShop.UserShop;

public record LoginResponse(String token, UserShop user) {}