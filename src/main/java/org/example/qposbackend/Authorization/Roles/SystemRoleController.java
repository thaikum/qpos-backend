package org.example.qposbackend.Authorization.Roles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("roles")
@RequiredArgsConstructor
public class SystemRoleController {
    private final SystemRoleRepository systemRoleRepository;
    private final SystemRoleService systemRoleService;

    @GetMapping
    public ResponseEntity<DataResponse> getRoles(){
        return ResponseEntity.ok(new DataResponse(systemRoleService.getRoles(), null));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> addRole(@RequestBody SystemRole systemRole){
        try{
            systemRoleRepository.save(systemRole);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Role added successfully"));
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new MessageResponse(ex.getMessage()));
        }
    }
}
