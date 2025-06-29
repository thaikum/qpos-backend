package org.example.qposbackend.InventoryItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;
import org.example.qposbackend.InventoryItem.PriceDetails.PriceDetails;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryItemServiceTest {
  @Mock private InventoryItemRepository inventoryItemRepository;
  @Mock private ItemService itemService;
  @Mock private ObjectMapper objectMapper;
  @Mock private MultipartFile multipartFile;
  @InjectMocks private InventoryItemService inventoryItemService;

  @BeforeEach
  public void setUp() {
    // Setup common behavior
  }

  @Test
  public void testCreateInventory_WithValidInput_ShouldCreateInventory() throws IOException {
    // Arrange
    InventoryItem inventoryItem = getDummyInventoryItem();
    Item item = getDummyItem();
    String stringifiedInventoryDTO = "{\"item\":{\"name\":\"Test Item\"}}";
    
    when(objectMapper.readValue(stringifiedInventoryDTO, InventoryItem.class))
        .thenReturn(inventoryItem);
    when(itemService.saveItem(any(Item.class), any(Optional.class)))
        .thenReturn(item);
    when(inventoryItemRepository.save(any(InventoryItem.class)))
        .thenReturn(inventoryItem);

    // Act
    inventoryItemService.createInventory(stringifiedInventoryDTO, Optional.of(multipartFile));

    // Assert
    verify(objectMapper).readValue(stringifiedInventoryDTO, InventoryItem.class);
    verify(itemService).saveItem(any(Item.class), any(Optional.class));
    verify(inventoryItemRepository).save(any(InventoryItem.class));
  }

  @Test
  public void testCreateInventory_WithInvalidJSON_ShouldThrowIOException() throws IOException {
    // Arrange
    String invalidJSON = "invalid json";
    when(objectMapper.readValue(invalidJSON, InventoryItem.class))
        .thenThrow(new IOException("Invalid JSON"));

    // Act & Assert
    assertThrows(IOException.class, () -> 
        inventoryItemService.createInventory(invalidJSON, Optional.empty()));
  }

  @Test
  public void testUpdateInventory_WithValidInput_ShouldUpdateInventory() throws IOException {
    // Arrange
    Long inventoryId = 1L;
    InventoryItem existingInventory = getDummyInventoryItem();
    existingInventory.setId(inventoryId);
    InventoryItem updatedInventory = getDummyInventoryItem();
    updatedInventory.setQuantity(100);
    Item updatedItem = getDummyItem();
    String formData = "{\"quantity\":100}";

    when(inventoryItemRepository.findById(inventoryId))
        .thenReturn(Optional.of(existingInventory));
    when(objectMapper.readValue(formData, InventoryItem.class))
        .thenReturn(updatedInventory);
    when(itemService.updateItem(anyLong(), any(Item.class), any(Optional.class)))
        .thenReturn(updatedItem);
    when(inventoryItemRepository.save(any(InventoryItem.class)))
        .thenReturn(existingInventory);

    // Act
    inventoryItemService.updateInventory(inventoryId, formData, Optional.of(multipartFile));

    // Assert
    verify(inventoryItemRepository).findById(inventoryId);
    verify(objectMapper).readValue(formData, InventoryItem.class);
    verify(itemService).updateItem(anyLong(), any(Item.class), any(Optional.class));
    verify(inventoryItemRepository).save(any(InventoryItem.class));
  }

  @Test
  public void testUpdateInventory_WithNonExistentId_ShouldThrowException() {
    // Arrange
    Long nonExistentId = 999L;
    String formData = "{\"quantity\":100}";
    
    when(inventoryItemRepository.findById(nonExistentId))
        .thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        inventoryItemService.updateInventory(nonExistentId, formData, Optional.empty()));
    
    assertEquals("Not found inventory item", exception.getMessage());
  }

  @Test
  public void testUpdateInventory_WithNewPriceDetails_ShouldUpdatePricesCorrectly() throws IOException {
    // Arrange
    Long inventoryId = 1L;
    InventoryItem existingInventory = getDummyInventoryItem();
    existingInventory.setId(inventoryId);
    
    // Create old price details with existing prices
    PriceDetails oldPriceDetails = getDummyPriceDetails();
    Price oldPrice = getDummyPrice();
    oldPrice.setStatus(PriceStatus.ACTIVE);
    oldPriceDetails.setPrices(List.of(oldPrice));
    existingInventory.setPriceDetails(oldPriceDetails);

    // Create new version with new price details
    InventoryItem newVersion = getDummyInventoryItem();
    PriceDetails newPriceDetails = getDummyPriceDetails();
    Price newPrice = getDummyPrice();
    newPrice.setSellingPrice(200.0);
    newPriceDetails.setPrices(List.of(newPrice));
    newVersion.setPriceDetails(newPriceDetails);

    String formData = "{\"priceDetails\":{\"prices\":[{\"sellingPrice\":200.0}]}}";

    when(inventoryItemRepository.findById(inventoryId))
        .thenReturn(Optional.of(existingInventory));
    when(objectMapper.readValue(formData, InventoryItem.class))
        .thenReturn(newVersion);
    when(itemService.updateItem(anyLong(), any(Item.class), any(Optional.class)))
        .thenReturn(getDummyItem());
    when(inventoryItemRepository.save(any(InventoryItem.class)))
        .thenReturn(existingInventory);

    // Act
    inventoryItemService.updateInventory(inventoryId, formData, Optional.empty());

    // Assert
    verify(inventoryItemRepository).save(argThat(savedInventory -> {
      PriceDetails savedPriceDetails = savedInventory.getPriceDetails();
      List<Price> savedPrices = savedPriceDetails.getPrices();
      
      // Should have both old and new prices
      boolean hasStoppedPrice = savedPrices.stream()
          .anyMatch(p -> p.getStatus() == PriceStatus.STOPPED);
      boolean hasNewPrice = savedPrices.stream()
          .anyMatch(p -> p.getSellingPrice() == 200.0);
      
      return hasStoppedPrice && hasNewPrice && savedPrices.size() == 2;
    }));
  }

  @Test
  public void testUpdateInventory_WithNullPriceDetails_ShouldNotUpdatePrices() throws IOException {
    // Arrange
    Long inventoryId = 1L;
    InventoryItem existingInventory = getDummyInventoryItem();
    existingInventory.setId(inventoryId);
    
    InventoryItem newVersion = getDummyInventoryItem();
    newVersion.setPriceDetails(null); // Null price details
    
    String formData = "{\"quantity\":50}";

    when(inventoryItemRepository.findById(inventoryId))
        .thenReturn(Optional.of(existingInventory));
    when(objectMapper.readValue(formData, InventoryItem.class))
        .thenReturn(newVersion);
    when(itemService.updateItem(anyLong(), any(Item.class), any(Optional.class)))
        .thenReturn(getDummyItem());
    when(inventoryItemRepository.save(any(InventoryItem.class)))
        .thenReturn(existingInventory);

    // Act
    inventoryItemService.updateInventory(inventoryId, formData, Optional.empty());

    // Assert
    verify(inventoryItemRepository).save(argThat(savedInventory -> 
        savedInventory.getPriceDetails() != null && 
        savedInventory.getPriceDetails().getPrices().size() == 1));
  }

  @Test
  public void testUpdateInventory_WithPartialUpdate_ShouldPreserveExistingValues() throws IOException {
    // Arrange
    Long inventoryId = 1L;
    InventoryItem existingInventory = getDummyInventoryItem();
    existingInventory.setId(inventoryId);
    existingInventory.setQuantity(50);
    existingInventory.setReorderLevel(10);
    
    InventoryItem partialUpdate = new InventoryItem();
    partialUpdate.setQuantity(100); // Only updating quantity
    
    String formData = "{\"quantity\":100}";

    when(inventoryItemRepository.findById(inventoryId))
        .thenReturn(Optional.of(existingInventory));
    when(objectMapper.readValue(formData, InventoryItem.class))
        .thenReturn(partialUpdate);
    when(itemService.updateItem(anyLong(), any(Item.class), any(Optional.class)))
        .thenReturn(getDummyItem());
    when(inventoryItemRepository.save(any(InventoryItem.class)))
        .thenReturn(existingInventory);

    // Act
    inventoryItemService.updateInventory(inventoryId, formData, Optional.empty());

    // Assert
    verify(inventoryItemRepository).save(argThat(savedInventory -> 
        savedInventory.getQuantity() == 100 && // Updated value
        savedInventory.getReorderLevel() == 10)); // Preserved value
  }
} 