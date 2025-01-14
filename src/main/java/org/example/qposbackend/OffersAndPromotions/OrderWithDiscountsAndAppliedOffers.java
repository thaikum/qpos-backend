package org.example.qposbackend.OffersAndPromotions;

import org.example.qposbackend.Order.SaleOrder;

import java.util.List;

public record OrderWithDiscountsAndAppliedOffers(SaleOrder saleOrder, List<AppliedOffersAndTotalDiscount> appliedOffersAndTotalDiscounts){}