package org.example.qposbackend.OffersAndPromotions;

import org.example.qposbackend.OffersAndPromotions.Offers.Offer;
import org.example.qposbackend.Order.OrderItem.OrderItem;

import java.util.List;

public record AppliedOffersAndTotalDiscount(Offer offer, Double totalDiscount, List<OrderItem> orderItem) {}