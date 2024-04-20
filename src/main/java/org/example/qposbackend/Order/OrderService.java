package org.example.qposbackend.Order;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.Order.OrderItem.OrderItem;
import org.example.qposbackend.Order.OrderItem.OrderItemRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public void processOrder(SaleOrder saleOrder){
        for(OrderItem orderItem: saleOrder.getOrderItems()){
            InventoryItem inventoryItem = orderItem.getInventoryItem();
            inventoryItem.setQuantity(inventoryItem.getQuantity() - orderItem.getQuantity());
            inventoryItem = inventoryItemRepository.save(inventoryItem);
            orderItem.setInventoryItem(inventoryItem);
        }

        orderRepository.save(saleOrder);
    }
}
