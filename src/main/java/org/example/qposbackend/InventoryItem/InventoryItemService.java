package org.example.qposbackend.InventoryItem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.lang.model.type.NullType;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;
import org.example.qposbackend.InventoryItem.PriceDetails.PriceDetails;
import org.example.qposbackend.InventoryItem.PriceDetails.PricingMode;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.ItemRepository;
import org.example.qposbackend.Item.ItemService;
import org.example.qposbackend.Item.UnitsOfMeasure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InventoryItemService {
  @Value("${files.resources}")
  private String resourcesDir;

  private final InventoryItemRepository inventoryItemRepository;
  private final ItemService itemService;

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

  //    @Bean
  public NullType balanceInventory() {
    try {
      List<InventoryItem> inventoryItems =
          inventoryItemRepository.findInventoryItemByIsDeleted(false);
      Map<String, List<InventoryItem>> inventoryItemMap =
          inventoryItems.stream().collect(Collectors.groupingBy(ii -> ii.getItem().getName()));

      String discrepanciesFileName = resourcesDir + "/bookkeeping/discrepancies.csv";
      List<String> lines = Files.readAllLines(Paths.get(discrepanciesFileName));
      lines.remove(0); // remove the title
      List<CalcBody> calcBodyList = new ArrayList<>();
      List<InventoryItem> modified = new ArrayList<>();

      for (String line : lines) {
        line = line.replace("\"", "");
        line = line.trim();
        String[] parts = line.split(",");
        CalcBody calcBody = new CalcBody();

        calcBody.setItemName(parts[0].trim());

        if (parts.length > 1) {
          int actual = Integer.parseInt(parts[1].trim());
          int expected = Integer.parseInt(parts[2].trim());

          calcBody.setQuantity(expected - actual);

          if (!inventoryItemMap.containsKey(parts[0].trim())) {
            System.out.println("Not found inventory item: " + parts[0]);
          } else {
            List<InventoryItem> list = inventoryItemMap.get(parts[0].trim());
            InventoryItem inventoryItem = list.get(0);

            if (inventoryItem.getQuantity() < 0) {
              inventoryItem.setQuantity(0);
            } else {
              inventoryItem.setQuantity(inventoryItem.getQuantity() - calcBody.getQuantity());
              calcBody.setBuyingPrice(inventoryItem.getBuyingPrice());
              calcBody.setSellingPrice(inventoryItem.getSellingPrice());
            }
            modified.add(inventoryItem);
          }
        }
        calcBodyList.add(calcBody);
      }
      inventoryItemRepository.saveAll(modified);

      // save the output to a file
      String outputFile = resourcesDir + "/bookkeeping/output.csv";
      lines = new ArrayList<>();
      for (CalcBody calcBody : calcBodyList) {
        String line =
            "%s,%d,%f,%f"
                .formatted(
                    calcBody.getItemName(),
                    Optional.ofNullable(calcBody.getQuantity()).orElse(0),
                    Optional.ofNullable(calcBody.getBuyingPrice()).orElse(0d),
                    Optional.ofNullable(calcBody.getSellingPrice()).orElse(0d));
        lines.add(line);
      }
      Files.write(Paths.get(outputFile), lines);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public InventoryItem createInventory(String stringfiedInventoryDTO, Optional<MultipartFile> image)
      throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    InventoryItem inventoryItem =
        objectMapper.readValue(stringfiedInventoryDTO, InventoryItem.class);

    Item item = itemService.saveItem(inventoryItem.getItem(), image);
    inventoryItem.setItem(item);
    return inventoryItemRepository.save(inventoryItem);
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
    inventoryItem.setPriceDetails(
        Optional.ofNullable(newVersion.getPriceDetails()).orElse(inventoryItem.getPriceDetails()));
    inventoryItemRepository.save(inventoryItem);
  }

  @DependsOn("categoryCreator")
  @Bean
  private NullType migrateItems() {
    List<InventoryItem> inventoryItems = inventoryItemRepository.findAll();

    for (InventoryItem inventoryItem : inventoryItems) {
      if (!Objects.isNull(inventoryItem.getPriceDetails())) continue;

      PriceDetails priceDetails = new PriceDetails();
      priceDetails.setPricingMode(PricingMode.CUSTOM_SELLING_PRICE);

      Price price = new Price();
      price.setStatus(PriceStatus.ACTIVE);
      price.setBuyingPrice(inventoryItem.getBuyingPrice());
      price.setSellingPrice(inventoryItem.getSellingPrice());
      price.setDiscountAllowed(inventoryItem.getDiscountAllowed());
      priceDetails.setPrices(List.of(price));

      inventoryItem.setPriceDetails(priceDetails);
    }

    inventoryItemRepository.saveAll(inventoryItems);
    return null;
  }
}

@Data
class CalcBody {
  private String itemName;
  private Integer quantity;
  private Double buyingPrice;
  private Double sellingPrice;
}
