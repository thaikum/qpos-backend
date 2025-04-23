package org.example.qposbackend.InventoryItem.PriceDetails.Price;

import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.Utils.EnumUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("price")
public class PriceController {
  @GetMapping("/price-status")
  public ResponseEntity<DataResponse> getPriceStatus() {
    return ResponseEntity.ok(new DataResponse(EnumUtils.toEnumList(PriceStatus.class), null));
  }
}
