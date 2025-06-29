package org.example.qposbackend.Authorization.User.userShop;

import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.Roles.SystemRoleRepository;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.shop.Shop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserShopServiceTest {
  @Mock private UserShopRepository userShopRepository;
  @Mock private SystemRoleRepository systemRoleRepository;
  @InjectMocks private UserShopService userShopService;

  private User mockUser;
  private Shop mockShop;
  private SystemRole mockOwnerRole;
  private SystemRole mockCustomRole;

  @BeforeEach
  public void setUp() {
    mockUser = getDummyUser();
    mockShop = getDummyShop();
    mockOwnerRole = getDummySystemRole();
    mockOwnerRole.setName("OWNER");
    mockCustomRole = getDummySystemRole();
    mockCustomRole.setName("MANAGER");
  }

  @Test
  public void testAddUserToShop_WithDefaultRole_ShouldAddUserWithOwnerRole() {
    // Arrange
    UserShop expectedUserShop = getDummyUserShop();
    expectedUserShop.setUser(mockUser);
    expectedUserShop.setShop(mockShop);
    expectedUserShop.setRole(mockOwnerRole);

    when(systemRoleRepository.findById("OWNER")).thenReturn(Optional.of(mockOwnerRole));
    when(userShopRepository.save(any(UserShop.class))).thenReturn(expectedUserShop);

    // Act
    UserShop result = userShopService.addUserToShop(mockUser, mockShop);

    // Assert
    assertNotNull(result);
    assertEquals(mockUser, result.getUser());
    assertEquals(mockShop, result.getShop());
    assertEquals(mockOwnerRole, result.getRole());
    
    verify(systemRoleRepository).findById("OWNER");
    verify(userShopRepository).save(argThat(userShop -> 
        userShop.getUser().equals(mockUser) &&
        userShop.getShop().equals(mockShop) &&
        userShop.getRole().equals(mockOwnerRole)));
  }

  @Test
  public void testAddUserToShop_WithOwnerRoleNotFound_ShouldThrowException() {
    // Arrange
    when(systemRoleRepository.findById("OWNER")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(RuntimeException.class, () -> 
        userShopService.addUserToShop(mockUser, mockShop));
    
    verify(systemRoleRepository).findById("OWNER");
    verify(userShopRepository, never()).save(any(UserShop.class));
  }

  @Test
  public void testAddUserToShop_WithCustomRole_ShouldAddUserWithSpecifiedRole() {
    // Arrange
    UserShop expectedUserShop = getDummyUserShop();
    expectedUserShop.setUser(mockUser);
    expectedUserShop.setShop(mockShop);
    expectedUserShop.setRole(mockCustomRole);

    when(userShopRepository.save(any(UserShop.class))).thenReturn(expectedUserShop);

    // Act
    UserShop result = userShopService.addUserToShop(mockUser, mockShop, mockCustomRole);

    // Assert
    assertNotNull(result);
    assertEquals(mockUser, result.getUser());
    assertEquals(mockShop, result.getShop());
    assertEquals(mockCustomRole, result.getRole());
    
    verify(userShopRepository).save(argThat(userShop -> 
        userShop.getUser().equals(mockUser) &&
        userShop.getShop().equals(mockShop) &&
        userShop.getRole().equals(mockCustomRole)));
    
    // Should not lookup role from repository when explicitly provided
    verify(systemRoleRepository, never()).findById(anyString());
  }

  @Test
  public void testAddUserToShop_WithNullUser_ShouldHandleGracefully() {
    // Arrange
    UserShop expectedUserShop = new UserShop();
    expectedUserShop.setUser(null);  
    expectedUserShop.setShop(mockShop);
    expectedUserShop.setRole(mockCustomRole);

    when(userShopRepository.save(any(UserShop.class))).thenReturn(expectedUserShop);

    // Act
    UserShop result = userShopService.addUserToShop(null, mockShop, mockCustomRole);

    // Assert
    assertNotNull(result);
    assertNull(result.getUser());
    assertEquals(mockShop, result.getShop());
    assertEquals(mockCustomRole, result.getRole());
    
    verify(userShopRepository).save(any(UserShop.class));
  }

  @Test
  public void testAddUserToShop_WithNullShop_ShouldHandleGracefully() {
    // Arrange
    UserShop expectedUserShop = new UserShop();
    expectedUserShop.setUser(mockUser);
    expectedUserShop.setShop(null);
    expectedUserShop.setRole(mockCustomRole);

    when(userShopRepository.save(any(UserShop.class))).thenReturn(expectedUserShop);

    // Act
    UserShop result = userShopService.addUserToShop(mockUser, null, mockCustomRole);

    // Assert
    assertNotNull(result);
    assertEquals(mockUser, result.getUser());
    assertNull(result.getShop());
    assertEquals(mockCustomRole, result.getRole());
    
    verify(userShopRepository).save(any(UserShop.class));
  }

  @Test
  public void testAddUserToShop_RepositorySaveFailure_ShouldPropagateException() {
    // Arrange
    RuntimeException repositoryException = new RuntimeException("Database error");
    when(userShopRepository.save(any(UserShop.class))).thenThrow(repositoryException);

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        userShopService.addUserToShop(mockUser, mockShop, mockCustomRole));
    
    assertEquals("Database error", exception.getMessage());
    verify(userShopRepository).save(any(UserShop.class));
  }
} 