package org.example.qposbackend.InventoryItem.PriceDetails;

import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.Utils.EnumUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("price-details")
public class PriceDetailsController {
    @GetMapping("/pricing-mode")
    public ResponseEntity<DataResponse> getPricingModes(){
        return ResponseEntity.ok(new DataResponse(EnumUtils.toEnumList(PricingMode.class), null));
    }
}
