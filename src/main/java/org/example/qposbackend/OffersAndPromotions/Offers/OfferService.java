package org.example.qposbackend.OffersAndPromotions.Offers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.DTOs.BundledConditionDTO;
import org.example.qposbackend.DTOs.OfferDTO;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.Item.ItemClassification.Category.Category;
import org.example.qposbackend.Item.ItemClassification.Category.CategoryRepository;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategory;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategoryRepository;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategory;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategoryRepository;
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
  private final CategoryRepository categoryRepository;
  private final MainCategoryRepository mainCategoryRepository;
  private final SubCategoryRepository subCategoryRepository;

  public void createOffer(OfferDTO offerDTO) {
    Offer offer = objectMapper.convertValue(offerDTO, Offer.class);

    log.info("{}", offerDTO);
    switch (offer.getEffectOn()) {
      case CATEGORIES ->
          offer.setCategories(categoryRepository.findAllById(offerDTO.getAffectedIds()));
      case MAIN_CATEGORIES ->
          offer.setMainCategories(mainCategoryRepository.findAllById(offerDTO.getAffectedIds()));
      case SUB_CATEGORIES ->
          offer.setSubCategories(subCategoryRepository.findAllById(offerDTO.getAffectedIds()));
      case ITEMS -> offer.setItems(inventoryItemRepository.findAllById(offerDTO.getAffectedIds()));
    }

    if (!Objects.isNull(offerDTO.getBundledConditions())) {
      List<BundledCondition> bundledConditions = new ArrayList<>();

      for (BundledConditionDTO bundledConditionDTO : offerDTO.getBundledConditions()) {
        BundledCondition bundledCondition =
            objectMapper.convertValue(bundledConditionDTO, BundledCondition.class);

        switch (offer.getBasedOn()) {
          case SUB_CATEGORIES ->
              bundledCondition.setSubCategory(
                  subCategoryRepository.getReferenceById(bundledConditionDTO.getValueId()));
          case CATEGORIES ->
              bundledCondition.setCategory(
                  categoryRepository.getReferenceById(bundledConditionDTO.getValueId()));
          case ITEMS ->
              bundledCondition.setItem(
                  inventoryItemRepository.getReferenceById(bundledConditionDTO.getValueId()));
          case MAIN_CATEGORIES ->
              bundledCondition.setMainCategory(
                  mainCategoryRepository.getReferenceById(bundledConditionDTO.getValueId()));
        }

        bundledConditions.add(bundledCondition);
      }

      offer.setBundledConditions(bundledConditions);
      offer.setActive(true);
      offerRepository.save(offer);
    }
  }

  public OrderWithDiscountsAndAppliedOffers getOffersToApply(SaleOrder order) {
    Set<Long> itemIds = new HashSet<>();
    Set<Long> categoryIds = new HashSet<>();
    Set<Long> mainCategoryIds = new HashSet<>();
    Set<Long> subCategoryIds = new HashSet<>();

    for (var orderItem : order.getOrderItems()) {
      InventoryItem inventoryItem =
          inventoryItemRepository.getReferenceById(orderItem.getInventoryItem().getId());
      itemIds.add(orderItem.getInventoryItem().getId());
      categoryIds.add(
          inventoryItem.getItem().getSubCategoryId().getCategory().getId());
      mainCategoryIds.add(
          inventoryItem
              .getItem()
              .getSubCategoryId()
              .getCategory()
              .getMainCategory()
              .getId());
      subCategoryIds.add(inventoryItem.getItem().getSubCategoryId().getId());
    }

    List<Offer> offers =
        offerRepository
            .findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(
                itemIds, categoryIds, mainCategoryIds, subCategoryIds);

    var free =
        offerRepository
            .findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInMainCategoriesIdsAndNotInSubCategoriesIds(
                itemIds, categoryIds, mainCategoryIds, subCategoryIds);
    offers.addAll(free);

    if (offers.isEmpty()) {
      System.out.println("Offers is empty");
      return new OrderWithDiscountsAndAppliedOffers(order, null);
    }

    List<AppliedOffersAndTotalDiscount> appliedOffersAndTotalDiscount = new ArrayList<>();

    for (Offer offer : offers) {
      List<OrderItem> orderItemsMeetingCriteria = new ArrayList<>();

      switch (offer.getEffectOn()) {
        case ITEMS ->
            orderItemsMeetingCriteria =
                order.getOrderItems().stream()
                    .filter(
                        orderItem ->
                            offer.getItems().stream()
                                .map(InventoryItem::getId)
                                .collect(Collectors.toSet())
                                .contains(orderItem.getInventoryItem().getId()))
                    .toList();

        case CATEGORIES ->
            orderItemsMeetingCriteria =
                order.getOrderItems().stream()
                    .filter(
                        orderItem ->
                            offer.getCategories().stream()
                                .map(Category::getId)
                                .collect(Collectors.toSet())
                                .contains(
                                    orderItem
                                        .getInventoryItem()
                                        .getItem()
                                        .getSubCategoryId()
                                        .getCategory()
                                        .getId()))
                    .toList();
        case MAIN_CATEGORIES ->
            orderItemsMeetingCriteria =
                order.getOrderItems().stream()
                    .filter(
                        orderItem ->
                            offer.getMainCategories().stream()
                                .map(MainCategory::getId)
                                .collect(Collectors.toSet())
                                .contains(
                                    orderItem
                                        .getInventoryItem()
                                        .getItem()
                                        .getSubCategoryId()
                                        .getCategory()
                                        .getMainCategory()
                                        .getId()))
                    .toList();
        case SUB_CATEGORIES ->
            orderItemsMeetingCriteria =
                order.getOrderItems().stream()
                    .filter(
                        orderItem ->
                            offer.getSubCategories().stream()
                                .map(SubCategory::getId)
                                .collect(Collectors.toSet())
                                .contains(
                                    orderItem
                                        .getInventoryItem()
                                        .getItem()
                                        .getSubCategoryId()
                                        .getId()))
                    .toList();

        case ALL -> orderItemsMeetingCriteria = order.getOrderItems();
      }

      var applied = getDiscounts(offer, order.getOrderItems(), orderItemsMeetingCriteria);
      if (applied != null) {
        if (orderItemsMeetingCriteria.isEmpty()) {
          List<Long> offerItemIds =
              Optional.of(offer.getItems()).orElse(new ArrayList<>()).stream()
                  .map(InventoryItem::getId)
                  .toList();
          List<Long> offerCategoryIds =
              Optional.of(offer.getCategories()).orElse(new ArrayList<>()).stream()
                  .map(Category::getId)
                  .toList();
          List<Long> offerSubCategoryIds =
              Optional.of(offer.getSubCategories()).orElse(new ArrayList<>()).stream()
                  .map(SubCategory::getId)
                  .toList();
          List<Long> offerMainCategoryIds =
              Optional.of(offer.getMainCategories()).orElse(new ArrayList<>()).stream()
                  .map(MainCategory::getId)
                  .toList();

          Optional<InventoryItem> cheapestItem =
              inventoryItemRepository
                  .findCheapestItemInInventoryIdsOrItemSubCategoryIdsOrItemCategoryIdsOrItemMainCategoryIds(
                      offerItemIds, offerCategoryIds, offerSubCategoryIds, offerMainCategoryIds);

          if (cheapestItem.isPresent()) {
            var inventoryItem = cheapestItem.get();
            OrderItem orderItem =
                OrderItem.builder()
                    .discount(inventoryItem.getSellingPrice())
                    .inventoryItem(inventoryItem)
                    .price(inventoryItem.getSellingPrice())
                    .quantity(1)
                    .offersApplied(List.of(offer))
                    .build();

            order.getOrderItems().add(orderItem);
            appliedOffersAndTotalDiscount.add(
                new AppliedOffersAndTotalDiscount(
                    offer, inventoryItem.getSellingPrice(), List.of(orderItem)));
          }
        } else {
          for (var item : order.getOrderItems()) {
            item.setDiscount(
                item.getDiscount() + (applied.totalDiscount() / order.getOrderItems().size()));
            List<Offer> offerOnItem =
                Optional.ofNullable(item.getOffersApplied()).orElse(new ArrayList<>());
            offerOnItem.add(offer);
            item.setOffersApplied(offerOnItem);
          }
          appliedOffersAndTotalDiscount.add(applied);
        }
      }
    }

    return new OrderWithDiscountsAndAppliedOffers(order, appliedOffersAndTotalDiscount);
  }

  private AppliedOffersAndTotalDiscount getDiscounts(
      Offer offer, List<OrderItem> orderItems, List<OrderItem> orderItemsMeetingCriteria) {
    switch (offer.getBasedOn()) {
      case CATEGORIES -> {
        Map<Long, List<OrderItem>> orderItemMap =
            orderItems.stream()
                .collect(
                    Collectors.groupingBy(
                        orderItem ->
                            orderItem
                                .getInventoryItem()
                                .getItem()
                                .getSubCategoryId()
                                .getCategory()
                                .getId()));
        for (BundledCondition bundledCondition : offer.getBundledConditions()) {
          if (doesNotMeetIndividualBundledCondition(
              bundledCondition,
              orderItemMap,
              bundledCondition.getSubCategory().getCategory().getId())) {
            return null;
          }
        }

        double totalDiscount = calculateDiscount(offer, orderItemsMeetingCriteria);
        return new AppliedOffersAndTotalDiscount(offer, totalDiscount, orderItemsMeetingCriteria);
      }
      case SUB_CATEGORIES -> {
        Map<Long, List<OrderItem>> orderItemMap =
            orderItems.stream()
                .collect(
                    Collectors.groupingBy(
                        orderItem ->
                            orderItem.getInventoryItem().getItem().getSubCategoryId().getId()));
        for (BundledCondition bundledCondition : offer.getBundledConditions()) {
          if (doesNotMeetIndividualBundledCondition(
              bundledCondition, orderItemMap, bundledCondition.getSubCategory().getId())) {
            return null;
          }
        }

        double totalDiscount = calculateDiscount(offer, orderItemsMeetingCriteria);
        return new AppliedOffersAndTotalDiscount(offer, totalDiscount, orderItemsMeetingCriteria);
      }
      case ITEMS -> {
        Map<Long, List<OrderItem>> orderItemMap =
            orderItems.stream()
                .collect(Collectors.groupingBy(orderItem -> orderItem.getInventoryItem().getId()));
        for (BundledCondition bundledCondition : offer.getBundledConditions()) {
          if (doesNotMeetIndividualBundledCondition(
              bundledCondition, orderItemMap, bundledCondition.getItem().getId())) {
            return null;
          }
        }

        double totalDiscount = calculateDiscount(offer, orderItemsMeetingCriteria);
        return new AppliedOffersAndTotalDiscount(offer, totalDiscount, orderItemsMeetingCriteria);
      }
      case MAIN_CATEGORIES -> {
        Map<Long, List<OrderItem>> orderItemMap =
            orderItems.stream()
                .collect(
                    Collectors.groupingBy(
                        orderItem ->
                            orderItem
                                .getInventoryItem()
                                .getItem()
                                .getSubCategoryId()
                                .getCategory()
                                .getMainCategory()
                                .getId()));
        for (BundledCondition bundledCondition : offer.getBundledConditions()) {
          if (doesNotMeetIndividualBundledCondition(
              bundledCondition, orderItemMap, bundledCondition.getMainCategory().getId())) {
            return null;
          }
        }

        double totalDiscount = calculateDiscount(offer, orderItemsMeetingCriteria);
        return new AppliedOffersAndTotalDiscount(offer, totalDiscount, orderItemsMeetingCriteria);
      }
      case QUANTITY -> {
        int totalItems = orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
        if (totalItems > offer.getMinQuantity()) {
          double totalDiscount = calculateDiscount(offer, orderItemsMeetingCriteria);
          return new AppliedOffersAndTotalDiscount(offer, totalDiscount, orderItemsMeetingCriteria);
        }
        return null;
      }
      case AMOUNT_SPENT -> {
        double totalSpent =
            orderItems.stream()
                .mapToDouble(
                    orderItem ->
                        (orderItem.getPrice() - orderItem.getDiscount()) - orderItem.getQuantity())
                .sum();
        if (totalSpent > offer.getMinAmount()) {
          double totalDiscount = calculateDiscount(offer, orderItemsMeetingCriteria);
          return new AppliedOffersAndTotalDiscount(offer, totalDiscount, orderItemsMeetingCriteria);
        }
        return null;
      }
      default -> {
        return null;
      }
    }
  }

  private boolean doesNotMeetIndividualBundledCondition(
      BundledCondition bundledCondition, Map<Long, List<OrderItem>> orderItemMap, Long key) {
    if (!orderItemMap.containsKey(key)) {
      return true;
    } else {
      if (!Objects.isNull(bundledCondition.getMinAmount())
          && bundledCondition.getMinAmount() != 0) {
        double totalSpent =
            orderItemMap.get(key).stream()
                .mapToDouble(
                    orderItem ->
                        (orderItem.getPrice() - orderItem.getDiscount()) * orderItem.getQuantity())
                .sum();
        if (totalSpent < bundledCondition.getMinAmount()) {
          return true;
        }
      } else if (!Objects.isNull(bundledCondition.getMinQuantity())
          && bundledCondition.getMinQuantity() != 0) {
        int totalItems = orderItemMap.get(key).stream().mapToInt(OrderItem::getQuantity).sum();
        if (totalItems < bundledCondition.getMinQuantity()) {
          return true;
        }
      } else {
        log.error("bundledCondition.getMinAmount() or BundledCondition.getMinQuantity() is false");
        return true;
      }
    }
    return false;
  }

  private Double calculateDiscount(Offer offer, Iterable<OrderItem> orderItems) {
    double totalDiscount = 0;

    for (var orderItem : orderItems) {
      if (totalDiscount != 0 && !offer.getApplyMultipleOnSameOrder()) break;

      Double discount =
          calculateDiscount(
              offer.getDiscountType(), offer.getDiscountAllowed(), orderItem.getPrice());
      totalDiscount += discount;

      if (!Objects.isNull(offer.getMaxDiscountPerOrder())) {
        totalDiscount =
            totalDiscount > offer.getMaxDiscountPerOrder()
                ? offer.getMaxDiscountPerOrder()
                : totalDiscount;
        break;
      }
    }
    return totalDiscount;
  }

  private Double calculateDiscount(
      DiscountType discountType, Double discountAllowed, Double price) {
    if (discountType == DiscountType.PERCENTAGE) {
      return discountAllowed * price / 100;
    } else {
      return discountAllowed;
    }
  }
}
