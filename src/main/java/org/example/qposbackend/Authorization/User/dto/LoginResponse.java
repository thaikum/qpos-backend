package org.example.qposbackend.Authorization.User.dto;

import org.example.qposbackend.Authorization.User.User;

public record LoginResponse(String token, User user) {}