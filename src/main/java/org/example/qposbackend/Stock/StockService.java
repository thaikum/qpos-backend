package org.example.qposbackend.Stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.DTOs.StockDTO;
import org.example.qposbackend.Exceptions.GenericExceptions;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.ItemRepository;
import org.example.qposbackend.Stock.StockItem.StockItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    public void addStock(StockDTO stockDTO) throws GenericExceptions {
        try {
            Stock stock = Stock.builder()
                    .arrivalDate(stockDTO.arrivalDate())
                    .purchaseDate(stockDTO.purchaseDate())
                    .transportCharges(stockDTO.transportCost())
                    .items(
                            stockDTO.items().stream().map(stockItemDTO -> {
                                        Optional<InventoryItem> itemOptional = inventoryItemRepository.findById(stockItemDTO.item());

                                        if (itemOptional.isPresent()) {
                                            InventoryItem item = itemOptional.get();
                                            item.setQuantity(item.getQuantity() + (stockItemDTO.quantity() * stockItemDTO.packaging()));
                                            item.setBuyingPrice(stockItemDTO.buyingPrice() / stockItemDTO.packaging());
                                            item = inventoryItemRepository.save(item);
                                            return StockItem.builder()
                                                    .buyingPrice(stockItemDTO.buyingPrice())
                                                    .packaging(stockItemDTO.packaging())
                                                    .quantity(stockItemDTO.quantity())
                                                    .item(item)
                                                    .build();
                                        } else {
                                            throw new RuntimeException("Item with id: " + stockItemDTO.item() + " is not present");
                                        }
                                    }

                            ).collect(Collectors.toList())
                    )
                    .build();

            this.stockRepository.save(stock);
        } catch (Exception ex) {
            throw new GenericExceptions(ex.getMessage());
        }
    }

    @Bean
    private void loadCsv() throws IOException {
        List<String> lines = Files.readAllLines(Path.of(resources +"/initial/merged_price_mapping.csv"));
        List<StockItem> stockItems = new ArrayList<>();

        if(!this.stockRepository.findAll().isEmpty()){
            return;
        }

        /*
        ['BAR CODE', ' ITEM NAME', 'MAIN CATEGORY', 'SUB CATEGORY', 'BRAND',
       'QUANTITY', 'PACKAGING', 'PRICE', 'Category', 'Quantity',
       'Buying Price', 'Selling Price'],
         */


        for(String line : lines) {
            String[] split = line.split(",");

            Item item = Item.builder()
                    .barCode(null)
                    .name(split[1].trim())
                    .category(split[2].trim())
                    .subCategory(split[3].trim())
                    .brand(split[4].trim())
                    .build();

            item = itemRepository.save(item);
            InventoryItem inventoryItem = InventoryItem.builder()
                    .item(item)
                    .buyingPrice(doubleParser(split[10].trim()))
                    .sellingPrice(doubleParser(split.length > 11 ? split[11].trim() : "0.0"))
                    .discountAllowed(0.0)
                    .quantity((int) Math.floor(doubleParser(split[9])))
                    .build();

            StockItem stockItem = StockItem.builder()
                    .item(inventoryItem)
                    .packaging(intParser(split[6]))
                    .quantity(inventoryItem.getQuantity())
                    .buyingPrice(inventoryItem.getBuyingPrice())
                    .build();

            stockItems.add(stockItem);
        }

        Stock stock = Stock.builder()
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
        try{
            return Double.parseDouble(line);
        }catch (Exception ignore){
            return 0.0;
        }
    }

    private Integer intParser(String line){
        try{
            return Integer.parseInt(line);
        }catch (Exception ignore){
            return 0;
        }
    }
}
