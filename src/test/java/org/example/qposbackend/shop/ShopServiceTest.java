package org.example.qposbackend.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Authorization.User.userShop.UserShopService;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.shop.dto.CreateShopInput;
import org.example.qposbackend.shop.dto.UpdateShopInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShopServiceTest {
  @Mock private ShopRepository shopRepository;
  @Mock private ObjectMapper objectMapper;
  @Mock private SpringSecurityAuditorAware auditorAware;
  @Mock private UserShopService userShopService;
  @InjectMocks private ShopService shopService;

  private UserShop mockUserShop;
  private User mockUser;
  private Shop mockShop;

  @BeforeEach
  public void setUp() {
    mockUserShop = getDummyUserShop();
    mockUser = getDummyUser();
    mockShop = getDummyShop();
    
    when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(mockUserShop));
  }

  @Test
  public void testGetShop_WithValidId_ShouldReturnShop() {
    // Arrange
    Long shopId = 1L;
    when(shopRepository.findById(shopId)).thenReturn(Optional.of(mockShop));

    // Act
    Shop result = shopService.getShop(shopId);

    // Assert
    assertNotNull(result);
    assertEquals(mockShop.getId(), result.getId());
    assertEquals(mockShop.getName(), result.getName());
    verify(shopRepository).findById(shopId);
  }

  @Test
  public void testGetShop_WithInvalidId_ShouldThrowException() {
    // Arrange
    Long invalidId = 999L;
    when(shopRepository.findById(invalidId)).thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        shopService.getShop(invalidId));
    
    assertEquals("Shop not found", exception.getMessage());
    verify(shopRepository).findById(invalidId);
  }

  @Test
  public void testGetAllShops_ShouldReturnAllShops() {
    // Arrange
    List<Shop> expectedShops = List.of(mockShop, getDummyShop());
    when(shopRepository.findAll()).thenReturn(expectedShops);

    // Act
    List<Shop> result = shopService.getAllShops();

    // Assert
    assertEquals(expectedShops.size(), result.size());
    assertEquals(expectedShops, result);
    verify(shopRepository).findAll();
  }

  @Test
  public void testCreateShop_WithValidInput_ShouldCreateShop() {
    // Arrange
    CreateShopInput input = new CreateShopInput();
    input.setName("New Shop");
    input.setPhone("+254712345678");
    input.setEmail("newshop@example.com");
    input.setAddress("123 Main Street");
    input.setLocation("Nairobi");
    input.setCurrency("KES");

    Shop newShop = getDummyShop();
    newShop.setName("New Shop");

    when(objectMapper.convertValue(input, Shop.class)).thenReturn(newShop);
    when(shopRepository.save(any(Shop.class))).thenReturn(newShop);
    doNothing().when(userShopService).addUserToShop(any(User.class), any(Shop.class));

    // Act
    Shop result = shopService.createShop(input);

    // Assert
    assertNotNull(result);
    assertEquals("New Shop", result.getName());
    verify(objectMapper).convertValue(input, Shop.class);
    verify(shopRepository, times(2)).save(any(Shop.class)); // Once for initial save, once for shop code save
    verify(userShopService).addUserToShop(mockUser, newShop);
  }

  @Test
  public void testCreateShop_WithNoCurrentUser_ShouldThrowException() {
    // Arrange
    CreateShopInput input = new CreateShopInput();
    input.setName("New Shop");

    when(auditorAware.getCurrentAuditor()).thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        shopService.createShop(input));
    
    assertEquals("User not found", exception.getMessage());
  }

  @Test
  public void testUpdateShop_WithValidInput_ShouldUpdateShop() {
    // Arrange
    Long shopId = 1L;
    UpdateShopInput input = new UpdateShopInput();
    input.setId(shopId);
    input.setName("Updated Shop Name");
    input.setPhone("+254700000000");
    input.setEmail("updated@example.com");
    input.setAddress("Updated Address");
    input.setLocation("Updated Location");
    input.setCurrency("USD");
    input.setActive(false);

    Shop existingShop = getDummyShop();
    existingShop.setId(shopId);

    when(shopRepository.findById(shopId)).thenReturn(Optional.of(existingShop));
    when(shopRepository.save(any(Shop.class))).thenReturn(existingShop);

    // Act
    Shop result = shopService.updateShop(input);

    // Assert
    assertNotNull(result);
    verify(shopRepository).findById(shopId);
    verify(shopRepository).save(argThat(shop -> 
        shop.getName().equals("Updated Shop Name") &&
        shop.getPhone().equals("+254700000000") &&
        shop.getEmail().equals("updated@example.com") &&
        shop.getAddress().equals("Updated Address") &&
        shop.getLocation().equals("Updated Location") &&
        shop.getCurrency().equals("USD") &&
        !shop.isActive()));
  }

  @Test
  public void testUpdateShop_WithPartialInput_ShouldUpdateOnlyProvidedFields() {
    // Arrange
    Long shopId = 1L;
    UpdateShopInput input = new UpdateShopInput();
    input.setId(shopId);
    input.setName("Updated Name Only");

    Shop existingShop = getDummyShop();
    existingShop.setId(shopId);
    String originalPhone = existingShop.getPhone();
    String originalEmail = existingShop.getEmail();

    when(shopRepository.findById(shopId)).thenReturn(Optional.of(existingShop));
    when(shopRepository.save(any(Shop.class))).thenReturn(existingShop);

    // Act
    Shop result = shopService.updateShop(input);

    // Assert
    assertNotNull(result);
    verify(shopRepository).save(argThat(shop -> 
        shop.getName().equals("Updated Name Only") &&
        shop.getPhone().equals(originalPhone) && // Should remain unchanged
        shop.getEmail().equals(originalEmail))); // Should remain unchanged
  }

  @Test
  public void testUpdateShop_WithNonExistentId_ShouldThrowException() {
    // Arrange
    Long nonExistentId = 999L;
    UpdateShopInput input = new UpdateShopInput();
    input.setId(nonExistentId);
    input.setName("Updated Name");

    when(shopRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        shopService.updateShop(input));
    
    assertEquals("Shop not found", exception.getMessage());
  }

  @Test
  public void testDeleteShop_WithValidShopCode_ShouldDeleteShop() {
    // Arrange
    String shopCode = "SHOP123";
    Shop deletedShop = getDummyShop();
    deletedShop.setDeleted(true);

    when(shopRepository.deleteShopByCode(shopCode)).thenReturn(deletedShop);

    // Act
    Shop result = shopService.deleteShop(shopCode);

    // Assert
    assertNotNull(result);
    verify(shopRepository).deleteShopByCode(shopCode);
  }

  @Test
  public void testDeleteShop_WithInvalidShopCode_ShouldReturnNull() {
    // Arrange
    String invalidCode = "INVALID";
    when(shopRepository.deleteShopByCode(invalidCode)).thenReturn(null);

    // Act
    Shop result = shopService.deleteShop(invalidCode);

    // Assert
    assertNull(result);
    verify(shopRepository).deleteShopByCode(invalidCode);
  }
} 