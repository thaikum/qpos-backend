package org.example.qposbackend.InventoryItem;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import javax.lang.model.type.NullType;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;
import org.example.qposbackend.InventoryItem.PriceDetails.PriceDetails;
import org.example.qposbackend.InventoryItem.PriceDetails.PricingMode;
import org.example.qposbackend.InventoryItem.quantityAdjustment.QuantityAdjustmentService;
import org.example.qposbackend.InventoryItem.quantityAdjustment.dto.QuantityAdjustmentDto;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.ItemRepository;
import org.example.qposbackend.Item.ItemService;
import org.example.qposbackend.Item.UnitsOfMeasure;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InventoryItemService {
  private final QuantityAdjustmentService quantityAdjustmentService;

  @Value("${files.resources}")
  private String resourcesDir;

  private final InventoryItemRepository inventoryItemRepository;
  private final ItemService itemService;
  private final SpringSecurityAuditorAware auditorAware;

  @Bean
  public NullType migrateInventoryItems(ItemRepository itemRepository) {
    List<InventoryItem> inventoryItems = inventoryItemRepository.findAll();

    for (InventoryItem inventoryItem : inventoryItems) {
      if (inventoryItem.getPriceDetails() != null) continue;

      PriceDetails priceDetails = new PriceDetails();
      priceDetails.setPricingMode(PricingMode.CUSTOM_SELLING_PRICE);
      Price price = new Price();
      price.setDiscountAllowed(inventoryItem.getDiscountAllowed());
      price.setQuantityUnderThisPrice(inventoryItem.getQuantity());
      price.setBuyingPrice(Optional.ofNullable(inventoryItem.getBuyingPrice()).orElse(0D));
      price.setSellingPrice(Optional.ofNullable(inventoryItem.getSellingPrice()).orElse(0D));
      price.setStatus(PriceStatus.ACTIVE);
      priceDetails.setPrices(List.of(price));
      inventoryItem.setPriceDetails(priceDetails);

      Item item = inventoryItem.getItem();
      item.setUnitOfMeasure(UnitsOfMeasure.PIECES);
      item.setMinimumPerUnit(1D);
      item = itemRepository.save(item);

      inventoryItem.setItem(item);
      inventoryItem.setReorderLevel(0);
      inventoryItem.setInventoryStatus(
          inventoryItem.isDeleted() ? InventoryStatus.INACTIVE : InventoryStatus.AVAILABLE);
      inventoryItemRepository.save(inventoryItem);
    }

    return null;
  }

  public List<InventoryItem> getInventoryItems() {
    UserShop userShop =
            auditorAware
                    .getCurrentAuditor()
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

    return inventoryItemRepository.findInventoryItemByShop_IdAndIsDeleted(userShop.getShop().getId(), false);
  }


  public void createInventory(String stringfiedInventoryDTO, Optional<MultipartFile> image)
      throws IOException {
    UserShop userShop =
            auditorAware
                    .getCurrentAuditor()
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

    ObjectMapper objectMapper = new ObjectMapper();
    InventoryItem inventoryItem =
        objectMapper.readValue(stringfiedInventoryDTO, InventoryItem.class);
    inventoryItem.setShop(userShop.getShop());

    Item item = itemService.saveItem(inventoryItem.getItem(), image);
    inventoryItem.setItem(item);
    inventoryItemRepository.save(inventoryItem);
  }

  public void updateInventory(Long id, String formData, Optional<MultipartFile> image)
      throws IOException {
    InventoryItem inventoryItem =
        inventoryItemRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Not found inventory item"));

    ObjectMapper objectMapper = new ObjectMapper();
    InventoryItem newVersion = objectMapper.readValue(formData, InventoryItem.class);
    Item item =
        itemService.updateItem(inventoryItem.getItem().getId(), newVersion.getItem(), image);

    inventoryItem.setItem(item);
    inventoryItem.setQuantity(
        Optional.ofNullable(newVersion.getQuantity()).orElse(inventoryItem.getQuantity()));
    inventoryItem.setInventoryStatus(
        Optional.ofNullable(newVersion.getInventoryStatus())
            .orElse(inventoryItem.getInventoryStatus()));
    inventoryItem.setSupplier(
        Optional.ofNullable(newVersion.getSupplier()).orElse(inventoryItem.getSupplier()));
    inventoryItem.setReorderLevel(
        Optional.ofNullable(newVersion.getReorderLevel()).orElse(inventoryItem.getReorderLevel()));

    if (!Objects.isNull(newVersion.getPriceDetails())) {
      var oldPriceDetails = inventoryItem.getPriceDetails();
      var newPriceDetails = newVersion.getPriceDetails();

      oldPriceDetails.setPricingMode(
          ObjectUtils.firstNonNull(
              newPriceDetails.getPricingMode(), oldPriceDetails.getPricingMode()));
      oldPriceDetails.setFixedProfit(
          ObjectUtils.firstNonNull(
              newPriceDetails.getFixedProfit(), oldPriceDetails.getFixedProfit()));
      oldPriceDetails.setProfitPercentage(
          ObjectUtils.firstNonNull(
              newPriceDetails.getProfitPercentage(), oldPriceDetails.getProfitPercentage()));

      if (!Objects.isNull(newPriceDetails.getPrices()) && !newPriceDetails.getPrices().isEmpty()) {
        List<Price> prices =
            new ArrayList<>(
                inventoryItem.getPriceDetails().getPrices().stream()
                    .peek(p -> p.setStatus(PriceStatus.STOPPED))
                    .toList());
        prices.addAll(newPriceDetails.getPrices());
        oldPriceDetails.setPrices(prices);
      }
    }
    inventoryItemRepository.save(inventoryItem);
  }

  public List<Price> updateInventoryItemPrice(Price price, Long id) {
    InventoryItem inventoryItem =
        inventoryItemRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Inventory Item not found"));
    Price lastPrice = inventoryItem.getPriceDetails().getPrices().getLast();
    lastPrice.setSellingPrice(price.getSellingPrice());
    lastPrice.setBuyingPrice(price.getBuyingPrice());
    lastPrice.setDiscountAllowed(price.getDiscountAllowed());
    inventoryItem = inventoryItemRepository.save(inventoryItem);
    return inventoryItem.getPriceDetails().getPrices();
  }

  @Transactional
  public InventoryItem updateInventoryItemQuantity(
      QuantityAdjustmentDto quantityAdjustmentDto, Long id) {
    InventoryItem inventoryItem =
        inventoryItemRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Inventory Item not found"));
    quantityAdjustmentService.createQuantityAdjustment(inventoryItem, quantityAdjustmentDto);
    inventoryItem
        .getPriceDetails()
        .adjustInventoryQuantity(quantityAdjustmentDto.quantity() - inventoryItem.getQuantity());
    return inventoryItemRepository.save(inventoryItem);
  }
}

@Data
class CalcBody {
  private String itemName;
  private Integer quantity;
  private Double buyingPrice;
  private Double sellingPrice;
}
