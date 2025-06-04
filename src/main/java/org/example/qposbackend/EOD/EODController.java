package org.example.qposbackend.EOD;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.EndOfDayDTO;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("eod")
@RequiredArgsConstructor
public class EODController {
    private final EODService eoDService;
    private final EODRepository eoDRepository;

    @PostMapping
    private ResponseEntity<MessageResponse> eod(@RequestBody EndOfDayDTO eodDTO) {
        eoDService.performEndOfDay(eodDTO);
        return ResponseEntity.ok(new MessageResponse("Successfully performed end of day"));
    }

    @PostMapping("/fetch-by-range")
    public ResponseEntity<DataResponse> fetchByRange(@RequestBody DateRange dateRange) {
        return ResponseEntity.ok(new DataResponse(eoDService.fetchByRange(dateRange), null));
    }
}
