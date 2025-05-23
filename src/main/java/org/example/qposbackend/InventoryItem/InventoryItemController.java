package org.example.qposbackend.InventoryItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.ItemService;
import org.example.qposbackend.Utils.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("inventory")
@Slf4j
public class InventoryItemController {
  private final InventoryItemRepository inventoryRepository;
  private final ItemService itemService;
  private final InventoryItemService inventoryItemService;

  @GetMapping
  public ResponseEntity<DataResponse> getInventoryItems() {
    try {
      return ResponseEntity.ok(
          new DataResponse(inventoryRepository.findInventoryItemByIsDeleted(false), null));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new DataResponse(null, ex.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<DataResponse> createInventoryItems(
      @RequestParam("inventoryEntry") String formData,
      @RequestPart("image") Optional<MultipartFile> image) {
    try {
      inventoryItemService.createInventory(formData, image);
      return ResponseEntity.ok(new DataResponse(null, "Inventory item created successfully"));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
          .body(new DataResponse(null, ex.getMessage()));
    }
  }

  @PutMapping("{id}")
  public ResponseEntity<MessageResponse> updateInventoryItems(
      @RequestParam("inventoryEntry") String formData,
      @RequestPart("image") Optional<MultipartFile> image,
      @PathVariable("id") Long id) {
    try {
      inventoryItemService.updateInventory(id, formData, image);
      return ResponseEntity.ok(new MessageResponse("Inventory item updated successfully"));
    } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
          .body(new MessageResponse(ex.getMessage()));
    }
  }

  @PutMapping("delete")
  public ResponseEntity<MessageResponse> deleteInventoryItem(
      @RequestBody InventoryItem inventoryItem) {
    inventoryRepository.markDelete(inventoryItem.getId());
    return ResponseEntity.ok(
        new MessageResponse(
            "Item: " + inventoryItem.getItem().getName() + " deleted successfully"));
  }

  @GetMapping("inventory-status")
  public ResponseEntity<DataResponse> getInventoryItemStatus() {
    return ResponseEntity.ok(new DataResponse(EnumUtils.toEnumList(InventoryStatus.class), null));
  }
}
