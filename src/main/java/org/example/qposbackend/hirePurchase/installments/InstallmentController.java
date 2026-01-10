package org.example.qposbackend.hirePurchase.installments;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.hirePurchase.installments.data.InstallmentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("hire-purchase/installments")
public class InstallmentController {
  private final InstallmentService installmentService;

  @PostMapping
  public ResponseEntity<DataResponse> addInstallment(
      @RequestBody @Valid InstallmentRequest installmentRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new DataResponse(installmentService.createInstallationAndBuildReceipt(installmentRequest), null));
  }
}
