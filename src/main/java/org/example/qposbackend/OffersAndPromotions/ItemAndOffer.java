package org.example.qposbackend.OffersAndPromotions;

import org.example.qposbackend.Order.OrderItem.OrderItem;

public record ItemAndOffer(OrderItem orderItem, AppliedOffersAndTotalDiscount appliedOffersAndTotalDiscount) {
}
