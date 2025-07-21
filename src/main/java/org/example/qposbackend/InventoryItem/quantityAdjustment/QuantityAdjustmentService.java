package org.example.qposbackend.InventoryItem.quantityAdjustment;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.quantityAdjustment.dto.QuantityAdjustmentDto;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class QuantityAdjustmentService {

  private final QuantityAdjustmentRepository quantityAdjustmentRepository;
  private final SpringSecurityAuditorAware auditorAware;

  public void createQuantityAdjustment(
          InventoryItem inventoryItem, QuantityAdjustmentDto adjustmentDto) {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    QuantityAdjustment quantityAdjustment =
        QuantityAdjustment.builder()
            .item(inventoryItem)
            .initialQuantity(inventoryItem.getQuantity())
            .adjustmentQuantity(adjustmentDto.quantity())
            .adjustmentReason(adjustmentDto.reason())
            .adjustedBy(userShop)
            .adjustedOn(LocalDateTime.now())
            .build();
    quantityAdjustmentRepository.save(quantityAdjustment);
  }
}
