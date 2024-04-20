package org.example.qposbackend.Authorization.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdType {
    NATIONAL_ID("NATIONAL IDENTITY"),
    ALIEN_ID("ALIEN ID"),
    DRIVING_LICENCE("DRIVING LICENCE"),
    PASSPORT("PASSPORT");

    private final String displayName;
}
