package org.example.qposbackend.order.orderItem.mappers;

import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.order.orderItem.data.OrderItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target="itemName", source = "inventoryItem.item.name")
    OrderItemResponse toResponse(OrderItem orderItem);
}
