package org.example.qposbackend.OffersAndPromotions.Offers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.DTOs.BundledConditionDTO;
import org.example.qposbackend.DTOs.OfferDTO;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.OffersAndPromotions.*;
import org.example.qposbackend.OffersAndPromotions.BundledConditions.BundledCondition;
import org.example.qposbackend.Order.OrderItem.OrderItem;
import org.example.qposbackend.Order.SaleOrder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {
    private final OfferRepository offerRepository;
    private final ObjectMapper objectMapper;
    private final InventoryItemRepository inventoryItemRepository;

    public void createOffer(OfferDTO offerDTO) {
        Offer offer = objectMapper.convertValue(offerDTO, Offer.class);

        if (!Objects.isNull(offerDTO.getBundledConditions())) {
            List<BundledCondition> bundledConditions = new ArrayList<>();
            for (BundledConditionDTO bundledConditionDTO : offerDTO.getBundledConditions()) {
                BundledCondition bundledCondition = objectMapper.convertValue(bundledConditionDTO, BundledCondition.class);
                if (!Objects.isNull(bundledConditionDTO.getItemId())) {
                    InventoryItem item = inventoryItemRepository.findById(bundledConditionDTO.getItemId()).orElseThrow();
                    bundledCondition.setItem(item);
                }
                bundledConditions.add(bundledCondition);
            }
            offer.setBundledConditions(bundledConditions);
        }

        if (!Objects.isNull(offerDTO.getItemsIds())) {
            List<InventoryItem> items = new ArrayList<>();
            for (var id : offerDTO.getItemsIds()) {
                InventoryItem item = inventoryItemRepository.findById(id).orElseThrow();
                items.add(item);
            }
            offer.setItems(items);
        }

        offerRepository.save(offer);
    }

    public OrderWithDiscountsAndAppliedOffers getOffersToApply(SaleOrder order) {
        List<InventoryItem> inventoryItems = new ArrayList<>();
        Map<Long, OrderItem> orderItems = new HashMap<>();

        for (var orderItem : order.getOrderItems()) {
            inventoryItems.add(orderItem.getInventoryItem());
            orderItems.put(orderItem.getInventoryItem().getId(), orderItem);
        }

        List<Offer> offers = offerRepository.findAllActiveOffersByItems(inventoryItems.stream().map(InventoryItem::getId).collect(Collectors.toList()));

        if (offers.isEmpty()) {
            return new OrderWithDiscountsAndAppliedOffers(order, null);
        }

        Map<Long, List<Offer>> itemsWithOffer = new HashMap<>();
        Map<String, List<Offer>> categoriesWithOffer = new HashMap<>();

        for (Offer offer : offers) {
            if (!offer.getItems().isEmpty()) {
                for (InventoryItem item : offer.getItems()) {
                    if (!itemsWithOffer.containsKey(item.getId())) {
                        itemsWithOffer.put(item.getId(), new ArrayList<>(List.of(offer)));
                    } else {
                        var cur = itemsWithOffer.get(item.getId());
                        cur.add(offer);
                    }
                }
            } else if (!Objects.isNull(offer.getCategory())) {
                categoriesWithOffer.put(offer.getCategory(), new ArrayList<>(List.of(offer)));
            }
        }

        List<ItemAndOffer> itemAndOffers = new ArrayList<>();

        for (var orderItem : order.getOrderItems()) {
            long itemId = orderItem.getInventoryItem().getId();
            String category = orderItem.getInventoryItem().getItem().getCategory();
            List<Offer> offersOnItem = itemsWithOffer.getOrDefault(itemId, categoriesWithOffer.getOrDefault(category, new ArrayList<>()));

            if (!offersOnItem.isEmpty()) {
                var offersAndTotalDiscount = calculateOffer(offers, orderItems, orderItem.getPrice());
                if (offersAndTotalDiscount.totalDiscount() != 0) {
                    orderItem.setDiscount(offersAndTotalDiscount.totalDiscount());
                    itemAndOffers.add(new ItemAndOffer(orderItem, offersAndTotalDiscount));
                }
            }
        }

        return new OrderWithDiscountsAndAppliedOffers(order, itemAndOffers);
    }

    private AppliedOffersAndTotalDiscount calculateOffer(List<Offer> offers, Map<Long, OrderItem> orderItems, Double price) {
        double discount = 0.0;
        List<Offer> appliedOffers = new ArrayList<>();


        for (Offer offer : offers) {
            Double offerDiscount = 0.0;
            if (offer.getOfferType() == OfferType.BUNDLED || offer.getOfferType() == OfferType.BUNDLED_CATEGORY) {
                boolean offerNotApplied = false;

                if (offer.getOfferType() == OfferType.BUNDLED) {
                    for (var bundledCondition : offer.getBundledConditions()) {
                        OrderItem orderItem = orderItems.get(bundledCondition.getItem().getId());
                        if (Objects.isNull(orderItem) || orderItem.getQuantity() < bundledCondition.getMinQuantity()) {
                            offerNotApplied = true;
                        }
                    }
                } else {
                    var categorizedItems = orderItems.values().stream()
                            .collect(Collectors.groupingBy(orderItem -> orderItem.getInventoryItem().getItem().getCategory()));

                    for (var bundledCondition : offer.getBundledConditions()) {
                        List<OrderItem> itemsInCategory = categorizedItems.get(bundledCondition.getCategory());
                        double minAmount = 0, minQuantity = 0;
                        for (OrderItem item : itemsInCategory) {
                            minQuantity += item.getQuantity();
                            minAmount += item.getQuantity() * item.getPrice();
                        }

                        if (!(minAmount != 0 && minAmount >= bundledCondition.getMinAmount() || minQuantity != 0 && minQuantity >= bundledCondition.getMinQuantity())) {
                            offerNotApplied = true;
                        }
                    }
                }

                if (!offerNotApplied) {
                    System.out.println("Here");

                    offerDiscount += calculateDiscount(offer.getDiscountType(), offer.getDiscountAllowed(), price);
                }

            } else if (offer.getOfferType() == OfferType.ALL || offer.getOfferType() == OfferType.CATEGORY_BASED || offer.getOfferType() == OfferType.ITEM_BASED) {
                offerDiscount += calculateDiscount(offer.getDiscountType(), offer.getDiscountAllowed(), price);
            } else if (offer.getOfferType() == OfferType.AMOUNT_BASED) {
                double total = orderItems.values().stream().mapToDouble(val -> (val.getPrice() - val.getDiscount()) * val.getQuantity()).sum();
                if (total >= offer.getMinAmount()) {
                    offerDiscount += calculateDiscount(offer.getDiscountType(), offer.getDiscountAllowed(), price);
                }
            }
            discount += offerDiscount;
            if (offerDiscount != 0) {
                appliedOffers.add(offer);
            }
        }

        return new AppliedOffersAndTotalDiscount(appliedOffers, discount);
    }

    private Double calculateDiscount(DiscountType discountType, Double value, Double price) {
        if (discountType == DiscountType.PERCENTAGE) {
            return value * price / 100;
        } else {
            return value;
        }
    }
}


