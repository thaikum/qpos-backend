package org.example.qposbackend.OffersAndPromotions;

import org.example.qposbackend.order.orderItem.OrderItem;

public record ItemAndOffer(OrderItem orderItem, AppliedOffersAndTotalDiscount appliedOffersAndTotalDiscount) {
}
