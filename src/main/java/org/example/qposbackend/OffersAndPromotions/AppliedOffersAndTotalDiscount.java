package org.example.qposbackend.OffersAndPromotions;

import org.example.qposbackend.OffersAndPromotions.Offers.Offer;

import java.util.List;

public record AppliedOffersAndTotalDiscount(List<Offer> offers, Double totalDiscount) {}