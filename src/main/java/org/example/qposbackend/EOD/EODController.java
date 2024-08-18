package org.example.qposbackend.EOD;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.EndOfDayDTO;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("eod")
@RequiredArgsConstructor
public class EODController {
    private final EODService eoDService;

    @PostMapping
    private ResponseEntity<MessageResponse> eod(@RequestBody EndOfDayDTO eodDTO) {
        eoDService.performEndOfDay(eodDTO);
        return ResponseEntity.ok(new MessageResponse("Successfully performed end of day"));
    }
}
