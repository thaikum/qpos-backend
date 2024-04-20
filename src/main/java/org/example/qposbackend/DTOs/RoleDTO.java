package org.example.qposbackend.DTOs;

import java.util.List;

public record RoleDTO(String name, List<PrivilegeDTO> privileges) {
}
