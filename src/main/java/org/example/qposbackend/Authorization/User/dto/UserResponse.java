package org.example.qposbackend.Authorization.User.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.qposbackend.Authorization.Roles.SystemRole;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserResponse extends UserDto {
    private Long id;
    private SystemRole systemRole;
    private Boolean isActive;
}
