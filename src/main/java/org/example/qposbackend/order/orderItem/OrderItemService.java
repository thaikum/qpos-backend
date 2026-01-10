package org.example.qposbackend.order.orderItem;

import graphql.util.Pair;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.order.orderItem.data.OrderItemRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {
  private final InventoryItemRepository inventoryItemRepository;
  private final OrderItemRepository orderItemRepository;

  public List<OrderItem> getOrderItems(
      List<OrderItemRequest> orderItemRequests, boolean checkAvailability) {
    List<OrderItem> orderItems = new ArrayList<>();

    for (OrderItemRequest orderItemRequest : orderItemRequests) {
      InventoryItem ii =
          inventoryItemRepository.getReferenceById(orderItemRequest.getInventoryItemId());

      if (ii.getQuantity() < orderItemRequest.getQuantity() && checkAvailability) {
        throw new RuntimeException("Not enough quantity in stock");
      }

      List<Pair<Double, Price>> quantityDeduction =
          ii.getPriceDetails()
              .getBuyingPriceBrokenDownPerTheQuantity(orderItemRequest.getQuantity());

      var builder =
          OrderItem.builder()
              .inventoryItem(ii)
              .price(ii.getPriceDetails().getSellingPrice())
              .discount(orderItemRequest.getDiscount());

      for (int x = 0; x < quantityDeduction.size(); x++) {
        Pair<Double, Price> pair = quantityDeduction.get(x);
        if (x == 0) {
          builder.quantity(pair.first);
          builder.buyingPrice(pair.second.getBuyingPrice());
        } else {
          builder.buyingPrice(pair.second.getBuyingPrice()).quantity(pair.first).build();
        }
        orderItems.add(builder.build());
      }
    }

    return orderItems;
  }

  @Transactional
  public List<OrderItem> createAndSaveOrderItems(
      List<OrderItemRequest> orderItemRequests, boolean checkAvailability) {
    return orderItemRepository.saveAll(getOrderItems(orderItemRequests, checkAvailability));
  }
}
