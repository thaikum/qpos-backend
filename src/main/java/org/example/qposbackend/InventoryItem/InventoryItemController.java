package org.example.qposbackend.InventoryItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("inventory")
@Slf4j
public class InventoryItemController {
    private final InventoryItemRepository inventoryRepository;
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<DataResponse> getInventoryItems() {
        try {
            return ResponseEntity.ok(new DataResponse(inventoryRepository.findAll(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DataResponse(null, ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<DataResponse> createInventoryItems(@RequestParam("inventoryEntry") String formData,
                                                             @RequestPart("image") Optional<MultipartFile> image){

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            InventoryItem inventoryItem = objectMapper.readValue(formData, InventoryItem.class);

            Item item = itemService.saveItem(inventoryItem.getItem(), image);
            inventoryItem.setItem(item);
            inventoryRepository.save(inventoryItem);

            return ResponseEntity.ok(new DataResponse(null, "Inventory item created successfully"));
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new DataResponse(null, ex.getMessage()));
        }

    }

    @PostMapping("item")
    public ResponseEntity<DataResponse> createItem(@RequestBody Item item){
        return ResponseEntity.ok(new DataResponse(null, "Item created successfully"));
    }
}
