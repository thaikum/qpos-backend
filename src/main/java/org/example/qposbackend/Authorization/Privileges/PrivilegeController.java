package org.example.qposbackend.Authorization.Privileges;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.PrivilegeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("privileges")
@RequiredArgsConstructor
public class PrivilegeController {
    private final PrivilegeRepository privilegeRepository;

    @GetMapping
    private ResponseEntity<DataResponse> getPrivileges() {
        return ResponseEntity.ok(new DataResponse(privilegeRepository.findAll().stream().map(privilege -> new PrivilegeDTO(privilege.getPrivilege().name(), privilege.getPrivilege().getDisplayName(), privilege.getPrivilege().getCategory())), null));
    }
}
