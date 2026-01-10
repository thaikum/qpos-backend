package org.example.qposbackend.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.example.qposbackend.Authorization.Privileges.RequirePrivilege;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.DTOs.ReturnItemRequest;
import org.example.qposbackend.order.receipt.ReceiptData;
import org.example.qposbackend.order.orderItem.ReturnInward.ReturnInwardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//
@RestController
@RequestMapping("order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
  private final OrderService orderService;
  private final ReturnInwardService returnInwardService;

  @PostMapping
  public ResponseEntity<?> createOrder(@RequestBody SaleOrder order) {
    SaleOrder saleOrder = orderService.processOrder(order);
    ReceiptData receiptData = orderService.generateReceipt(saleOrder.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse(receiptData, null));
  }

  @PostMapping("get-by-range")
  public ResponseEntity<DataResponse> fetchByDateRange(@RequestBody DateRange dateRange) {
    return ResponseEntity.ok(new DataResponse(orderService.fetchByDateRange(dateRange), null));
  }

  @PostMapping("return-item")
  public ResponseEntity<MessageResponse> returnItem(
      @RequestBody ReturnItemRequest returnItemRequest) {
    returnInwardService.returnItem(returnItemRequest);
    return ResponseEntity.ok(new MessageResponse("Item returned successfully"));
  }

  @PostMapping("generate-receipt/{orderId}")
  @RequirePrivilege(PrivilegesEnum.MAKE_SALE)
  public ResponseEntity<DataResponse> generateReceipt(@PathVariable("orderId") Long orderId) {
    ReceiptData receiptData = orderService.generateReceipt(orderId);
    return ResponseEntity.ok(new DataResponse(receiptData, null));
  }
}
