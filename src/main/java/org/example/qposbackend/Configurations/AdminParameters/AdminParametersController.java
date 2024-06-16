package org.example.qposbackend.Configurations.AdminParameters;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin-parameters")
@RequiredArgsConstructor
public class AdminParametersController {

    private final AdminParametersRepository adminParametersRepository;

    @PutMapping
    public ResponseEntity<MessageResponse> updateAdminParameters(@RequestBody AdminParameters adminParameters) {
        try{
            adminParametersRepository.save(adminParameters);
            return ResponseEntity.ok(new MessageResponse("Successfully updated admin parameters"));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<DataResponse> getAdminParameters() {
        return ResponseEntity.ok(new DataResponse(adminParametersRepository.findAll(), null));
    }
}
