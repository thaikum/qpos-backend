package org.example.qposbackend.Stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.DTOs.StockDTO;
import org.example.qposbackend.DTOs.StockItemDTO;
import org.example.qposbackend.Exceptions.GenericExceptions;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;
import org.example.qposbackend.InventoryItem.PriceDetails.PricingMode;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.ItemRepository;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.Stock.StockItem.StockItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {
  private final StockRepository stockRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final ItemRepository itemRepository;

  @Value("${files.resources}")
  private String resources;

  private final SpringSecurityAuditorAware auditorAware;

  public List<Stock> getAllShopStock() {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    return stockRepository.findAllByShop(userShop.getShop());
  }

  public void addStock(StockDTO stockDTO) throws GenericExceptions {
    UserShop userShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new NoSuchElementException("User not found"));
    try {
      Stock.StockBuilder builder = Stock.builder();
      builder.arrivalDate(stockDTO.arrivalDate());
      builder.purchaseDate(stockDTO.purchaseDate());
      builder.transportCharges(stockDTO.transportCost());
      builder.items(
          stockDTO.items().stream()
              .map(
                  stockItemDTO -> {
                    Optional<InventoryItem> itemOptional =
                        inventoryItemRepository.findById(stockItemDTO.item());

                    if (itemOptional.isPresent()) {
                      InventoryItem item = itemOptional.get();

                      setNewPricesAndQuantity(stockItemDTO, item);
                      item = inventoryItemRepository.save(item);
                      return StockItem.builder()
                          .buyingPrice(stockItemDTO.buyingPrice())
                          .packaging(stockItemDTO.packaging())
                          .quantity(stockItemDTO.quantity())
                          .item(item)
                          .build();
                    } else {
                      throw new RuntimeException(
                          "Item with id: " + stockItemDTO.item() + " is not present");
                    }
                  })
              .collect(Collectors.toList()));
      builder.shop(userShop.getShop());
      Stock stock = builder.build();

      this.stockRepository.save(stock);
    } catch (Exception ex) {
      throw new GenericExceptions(ex.getMessage());
    }
  }

  private static void setNewPricesAndQuantity(StockItemDTO stockItemDTO, InventoryItem item) {
    Optional<Price> latestPriceOptional =
        item.getPriceDetails().getPrices().stream()
            .max(Comparator.comparing(Price::getCreationTimestamp));

    if (latestPriceOptional.isPresent()) {
      Price latestPrice = latestPriceOptional.get();
      if (latestPrice.getBuyingPrice() == stockItemDTO.buyingPrice() / stockItemDTO.packaging()) {
        latestPrice.setQuantityUnderThisPrice(
            stockItemDTO.quantity() * stockItemDTO.packaging()
                + latestPrice.getQuantityUnderThisPrice());
      } else {
        Price price = new Price();
        price.setBuyingPrice(stockItemDTO.buyingPrice() / stockItemDTO.packaging());
        price.setQuantityUnderThisPrice(stockItemDTO.quantity() * stockItemDTO.packaging());

        if (item.getPriceDetails().getPricingMode() == PricingMode.PERCENTAGE) {
          Double profitPercentage = item.getPriceDetails().getProfitPercentage();
          price.setSellingPrice(
              stockItemDTO.buyingPrice() / stockItemDTO.packaging()
                  + (stockItemDTO.buyingPrice()
                      / stockItemDTO.packaging()
                      * profitPercentage
                      / 100));
        } else if (item.getPriceDetails().getPricingMode() == PricingMode.FIXED_PROFIT) {
          Double profit = item.getPriceDetails().getFixedProfit();
          price.setSellingPrice(stockItemDTO.buyingPrice() / stockItemDTO.packaging() + profit);
        } else {
          price.setSellingPrice(latestPrice.getSellingPrice());
        }
        price.setDiscountAllowed(latestPrice.getDiscountAllowed());

        Optional<Price> activePriceOptional =
            item.getPriceDetails().getPrices().stream()
                .filter(v -> v.getStatus() == PriceStatus.ACTIVE)
                .findFirst();
        if (activePriceOptional.isPresent()) {
          Price activePrice = activePriceOptional.get();
          if (activePrice.getSellingPrice() > price.getSellingPrice()) {
            price.setStatus(PriceStatus.FUTURE);
          } else {
            price.setStatus(PriceStatus.ACTIVE);
            activePrice.setStatus(PriceStatus.STOPPED);
            activePrice.setStoppedOnTimestamp(new Date());
          }
        }
        item.getPriceDetails().getPrices().add(price);
      }
    } else {
      Price price = createNewPrice(stockItemDTO, item);
      item.getPriceDetails().setPrices(List.of(price));
    }
  }

  private static Price createNewPrice(StockItemDTO stockItemDTO, InventoryItem item) {
    Price price = new Price();
    price.setBuyingPrice(stockItemDTO.buyingPrice() / stockItemDTO.packaging());

    switch (item.getPriceDetails().getPricingMode()) {
      case FIXED_PROFIT ->
          price.setSellingPrice(
              stockItemDTO.buyingPrice() / stockItemDTO.packaging()
                  + item.getPriceDetails().getFixedProfit());
      case PERCENTAGE ->
          price.setSellingPrice(
              stockItemDTO.buyingPrice() / stockItemDTO.packaging()
                  + (stockItemDTO.buyingPrice()
                      / stockItemDTO.packaging()
                      * item.getPriceDetails().getProfitPercentage()
                      / 100));
      case CUSTOM_SELLING_PRICE ->
          price.setSellingPrice(
              stockItemDTO.buyingPrice()
                  / stockItemDTO.packaging()
                  * 2); // todo use a global profit percentage
    }
    price.setDiscountAllowed(0);
    price.setStatus(PriceStatus.ACTIVE);
    price.setQuantityUnderThisPrice(stockItemDTO.quantity() * stockItemDTO.packaging());
    return price;
  }

  //    @Bean
  private void loadCsv() throws IOException {
    List<String> lines =
        Files.readAllLines(Path.of(resources + "/initial/merged_price_mapping.csv"));
    List<StockItem> stockItems = new ArrayList<>();

    if (!this.stockRepository.findAll().isEmpty()) {
      return;
    }

    for (String line : lines) {
      String[] split = line.split(",");

      Item item =
          Item.builder()
              .barCode(null)
              .name(split[1].trim())
              .category(split[2].trim())
              .subCategory(split[3].trim())
              .brand(split[4].trim())
              .build();

      item = itemRepository.save(item);
      InventoryItem inventoryItem =
          InventoryItem.builder()
              .item(item)
              .buyingPrice(doubleParser(split[10].trim()))
              .sellingPrice(doubleParser(split.length > 11 ? split[11].trim() : "0.0"))
              .discountAllowed(0.0)
              .quantity((int) Math.floor(doubleParser(split[9])))
              .build();

      StockItem stockItem =
          StockItem.builder()
              .item(inventoryItem)
              .packaging(intParser(split[6]))
              .quantity(inventoryItem.getQuantity())
              .buyingPrice(inventoryItem.getBuyingPrice())
              .build();

      stockItems.add(stockItem);
    }

    Stock stock =
        Stock.builder()
            .arrivalDate(new Date(1709996509000L))
            .purchaseDate(new Date(1709996509000L))
            .transportCharges(4000.0)
            .otherCostsIncurred(0.0)
            .items(stockItems)
            .build();

    stockRepository.save(stock);

    log.info("Done loading all initial stock");
  }

  private Double doubleParser(String line) {
    try {
      return Double.parseDouble(line);
    } catch (Exception ignore) {
      return 0.0;
    }
  }

  private Integer intParser(String line) {
    try {
      return Integer.parseInt(line);
    } catch (Exception ignore) {
      return 0;
    }
  }
}
