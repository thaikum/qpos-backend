package org.example.qposbackend.Accounting.Transactions.TranHeader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.DTOs.TranHeaderDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("transactions")
@Slf4j
@RestController
@RequiredArgsConstructor
public class TranHeaderController {
    private final TranHeaderService tranHeaderService;
    private final TranHeaderRepository tranHeaderRepository;

    @PostMapping
    public ResponseEntity<MessageResponse> createTranHeader(@RequestBody TranHeaderDTO tranHeaderDTO) {
        try {
            tranHeaderService.createTransactions(tranHeaderDTO);
            return ResponseEntity.ok(new MessageResponse("Transaction successfully created, wait for verification"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<DataResponse> getAllTranHeaders() {
        return ResponseEntity.ok(new DataResponse(tranHeaderService.fetchTransactions(), null));
    }

}
