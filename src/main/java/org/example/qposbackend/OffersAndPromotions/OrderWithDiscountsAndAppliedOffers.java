package org.example.qposbackend.OffersAndPromotions;

import org.example.qposbackend.Order.OrderItem.OrderItem;
import org.example.qposbackend.Order.SaleOrder;

import java.util.List;
import java.util.Map;

public record OrderWithDiscountsAndAppliedOffers(SaleOrder saleOrder, List<ItemAndOffer> itemAndOfferList){}