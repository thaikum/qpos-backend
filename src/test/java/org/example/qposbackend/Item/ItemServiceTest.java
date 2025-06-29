package org.example.qposbackend.Item;

import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategory;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
  @Mock private SubCategoryRepository subCategoryRepository;
  @Mock private ItemRepository itemRepository;
  @Mock private MultipartFile multipartFile;
  @InjectMocks private ItemService itemService;

  private Item mockItem;
  private SubCategory mockSubCategory;

  @BeforeEach
  public void setUp() {
    mockItem = getDummyItem();
    mockSubCategory = mock(SubCategory.class);
    when(mockSubCategory.getId()).thenReturn(1L);
    
    // Set upload path using reflection
    ReflectionTestUtils.setField(itemService, "uploadPath", "/test/upload/path/");
  }

  @Test
  public void testSaveItem_WithValidItem_ShouldSaveItem() throws IOException {
    // Arrange
    mockItem.setSubCategory("1");
    mockItem.setId(1L);
    
    when(subCategoryRepository.findById(1L)).thenReturn(Optional.of(mockSubCategory));
    when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

    // Act
    Item result = itemService.saveItem(mockItem, Optional.empty());

    // Assert
    assertNotNull(result);
    assertEquals(mockItem.getId(), result.getId());
    verify(subCategoryRepository).findById(1L);
    verify(itemRepository, times(2)).save(any(Item.class)); // Called twice: before and after image processing
  }

  @Test
  public void testSaveItem_WithInvalidSubCategory_ShouldThrowException() {
    // Arrange
    mockItem.setSubCategory("999");
    
    when(subCategoryRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(RuntimeException.class, () -> 
        itemService.saveItem(mockItem, Optional.empty()));
    
    verify(subCategoryRepository).findById(999L);
    verify(itemRepository, never()).save(any(Item.class));
  }

  @Test
  public void testSaveItem_WithEmptyBarcode_ShouldSetBarcodeToNull() throws IOException {
    // Arrange
    mockItem.setSubCategory("1");
    mockItem.setBarCode("   "); // Empty/whitespace barcode
    
    when(subCategoryRepository.findById(1L)).thenReturn(Optional.of(mockSubCategory));
    when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
      Item savedItem = invocation.getArgument(0);
      return savedItem;
    });

    // Act
    itemService.saveItem(mockItem, Optional.empty());

    // Assert
    verify(itemRepository, times(2)).save(argThat(item -> item.getBarCode() == null));
  }

  @Test
  public void testUpdateItem_WithValidItem_ShouldUpdateItem() throws IOException {
    // Arrange
    Long itemId = 1L;
    Item existingItem = getDummyItem();
    existingItem.setId(itemId);
    existingItem.setName("Old Name");
    
    Item updateItem = getDummyItem();
    updateItem.setName("New Name");
    updateItem.setSubCategory("2");
    
    SubCategory newSubCategory = mock(SubCategory.class);
    when(newSubCategory.getId()).thenReturn(2L);

    when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
    when(subCategoryRepository.findById(2L)).thenReturn(Optional.of(newSubCategory));
    when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

    // Act
    Item result = itemService.updateItem(itemId, updateItem, Optional.empty());

    // Assert
    assertNotNull(result);
    verify(itemRepository).findById(itemId);
    verify(subCategoryRepository).findById(2L);
    verify(itemRepository).save(argThat(item -> 
        item.getName().equals("New Name") &&
        item.getSubCategoryId().equals(newSubCategory)));
  }

  @Test
  public void testUpdateItem_WithNonExistentId_ShouldThrowException() {
    // Arrange
    Long nonExistentId = 999L;
    Item updateItem = getDummyItem();
    
    when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        itemService.updateItem(nonExistentId, updateItem, Optional.empty()));
    
    assertEquals("Item not found", exception.getMessage());
    verify(itemRepository).findById(nonExistentId);
  }

  @Test
  public void testServeImage_WithNonExistentImage_ShouldThrowFileNotFoundException() {
    // Arrange
    String nonExistentImage = "nonexistent.jpg";

    // Act & Assert
    assertThrows(FileNotFoundException.class, () -> 
        itemService.serveImage(nonExistentImage));
  }
} 