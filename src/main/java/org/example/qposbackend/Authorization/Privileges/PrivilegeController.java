package org.example.qposbackend.Authorization.Privileges;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.PrivilegeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("privileges")
@RequiredArgsConstructor
public class PrivilegeController {
    private final PrivilegeRepository privilegeRepository;

    @GetMapping
    private ResponseEntity<DataResponse> getPrivileges() {
        return ResponseEntity.ok(new DataResponse(privilegeRepository.findAll().stream().map(p -> {
            PrivilegesEnum privilege = PrivilegesEnum.valueOf(p.getPrivilege());
            return new PrivilegeDTO(privilege.name(), privilege.getDisplayName(), privilege.getCategory());
        }), null));
    }
}
