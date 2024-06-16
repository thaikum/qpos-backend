package org.example.qposbackend.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//
@RestController
@RequestMapping("order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<MessageResponse> createOrder(@RequestBody SaleOrder order) {
        try {
            orderService.processOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Order created successfully"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new MessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("get-by-range")
    public ResponseEntity<DataResponse> fetchByDateRange(@RequestBody DateRange dateRange) {
        log.info("{}", dateRange);
        return ResponseEntity.ok(new DataResponse(orderRepository.fetchAllByDateRange(dateRange.start(), dateRange.end()), null));
    }
}
