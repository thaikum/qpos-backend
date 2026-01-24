package org.example.qposbackend.Accounting.Transactions.TranHeader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.TranHeader.data.TranHeaderResponseDTO;
import org.example.qposbackend.Accounting.Transactions.TranHeader.mappers.TranHeaderMapper;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.DTOs.TranHeaderDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("transactions")
@Slf4j
@RestController
@RequiredArgsConstructor
public class TranHeaderController {
  private final TranHeaderService tranHeaderService;
  private final TranHeaderRepository tranHeaderRepository;

  @PostMapping
  public ResponseEntity<MessageResponse> createTranHeader(
      @RequestBody TranHeaderDTO tranHeaderDTO) {
    try {
      tranHeaderService.createTransactions(tranHeaderDTO);
      return ResponseEntity.ok(
          new MessageResponse("Transaction successfully created, wait for verification"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
          .body(new MessageResponse(e.getMessage()));
    }
  }

  @PostMapping("by-range/{status}")
  public ResponseEntity<DataResponse> getAllTranHeaders(
      @RequestBody DateRange dateRange, @PathVariable String status) {
    return ResponseEntity.ok(
        new DataResponse(tranHeaderService.fetchTransactionsByRange(dateRange, status), null));
  }

  @PutMapping("verify")
  public ResponseEntity<MessageResponse> verifyTranHeaders(@RequestBody List<Long> ids) {
    try {
      List<TranHeader> tranHeaders = tranHeaderRepository.findAllById(ids);
      tranHeaderService.verifyTransactions(tranHeaders);
      return ResponseEntity.ok(new MessageResponse("Transaction verified successfully"));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
          .body(new MessageResponse(e.getMessage()));
    }
  }

  @PutMapping("decline")
  public ResponseEntity<MessageResponse> declineTranHeaders(@RequestBody List<Long> ids) {
    tranHeaderService.declineTransactions(ids);
    return ResponseEntity.ok(new MessageResponse("Transaction verified successfully"));
  }

  @GetMapping("{id}")
  public ResponseEntity<DataResponse> getTransaction(@PathVariable Long id) {
    try {
      Optional<TranHeader> tranHeader = tranHeaderRepository.findById(id);
      if (tranHeader.isPresent()) {
        TranHeaderResponseDTO responseDTO = TranHeaderMapper.toResponseDTO(tranHeader.get());
        return ResponseEntity.ok(new DataResponse(responseDTO, null));
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new DataResponse(null, "Transaction not found"));
      }
    } catch (Exception e) {
      log.error("Error fetching transaction: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new DataResponse(null, e.getMessage()));
    }
  }
}
