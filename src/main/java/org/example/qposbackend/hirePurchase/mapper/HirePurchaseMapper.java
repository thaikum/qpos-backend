package org.example.qposbackend.hirePurchase.mapper;

import org.example.qposbackend.hirePurchase.HirePurchase;
import org.example.qposbackend.hirePurchase.data.HirePurchaseResponse;
import org.example.qposbackend.order.orderItem.mappers.OrderItemMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface HirePurchaseMapper {
  @Mapping(target = "customerName", source = "customer.fullName")
  @Mapping(target = "customerId", source = "customer.id")
  HirePurchaseResponse toResponse(HirePurchase hirePurchase);
}
