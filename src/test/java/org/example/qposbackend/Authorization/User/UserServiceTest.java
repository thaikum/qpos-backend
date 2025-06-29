package org.example.qposbackend.Authorization.User;

import org.example.qposbackend.Authorization.Roles.SystemRoleRepository;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.example.qposbackend.Authorization.User.Password.Password;
import org.example.qposbackend.Authorization.User.dto.LoginResponse;
import org.example.qposbackend.Authorization.User.dto.UserCredentials;
import org.example.qposbackend.DTOs.AuthRequest;
import org.example.qposbackend.DTOs.PasswordChange;
import org.example.qposbackend.Security.Jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  @Mock private JwtUtil jwtUtil;
  @Mock private UserRepository userRepository;
  @Mock private BCryptPasswordEncoder passwordEncoder;
  @Mock private SystemRoleRepository systemRoleRepository;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private Authentication authentication;
  @Mock private SecurityContext securityContext;
  @InjectMocks private UserService userService;

  private User mockUser;
  private SystemUserDetails mockUserDetails;

  @BeforeEach
  public void setUp() {
    mockUser = getDummyUser();
    mockUserDetails = mock(SystemUserDetails.class);
    when(mockUserDetails.getUsername()).thenReturn(mockUser.getEmail());
  }

  @Test
  public void testAuthenticateUser_WithValidCredentials_ShouldReturnLoginResponse() {
    // Arrange
    AuthRequest authRequest = new AuthRequest("test@example.com", "password123", "SHOP001");
    String expectedToken = "jwt.token.here";
    
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUserDetails);
    when(userRepository.findUserByEmail(authRequest.email())).thenReturn(Optional.of(mockUser));
    when(jwtUtil.generateToken(mockUserDetails)).thenReturn(expectedToken);

    // Act
    LoginResponse result = userService.authenticateUser(authRequest);

    // Assert
    assertNotNull(result);
    assertEquals(expectedToken, result.token());
    assertEquals(mockUser, result.user());
    
    verify(authenticationManager).authenticate(argThat(auth -> {
        UserCredentials credentials = (UserCredentials) auth.getPrincipal();
      return credentials.email().equals(authRequest.email()) &&
             credentials.shopCode().equals(authRequest.shopCode()) &&
             auth.getCredentials().equals(authRequest.password());
    }));
    verify(jwtUtil).generateToken(mockUserDetails);
  }

  @Test
  public void testAuthenticateUser_WithInvalidCredentials_ShouldThrowException() {
    // Arrange
    AuthRequest authRequest = new AuthRequest("invalid@example.com", "wrongpassword", "SHOP001");
    
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new RuntimeException("Authentication failed"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        userService.authenticateUser(authRequest));
    
    assertEquals("Authentication failed", exception.getMessage());
  }

  @Test
  public void testAuthenticateUser_WithNonExistentUser_ShouldThrowException() {
    // Arrange
    AuthRequest authRequest = new AuthRequest("nonexistent@example.com", "password123", "SHOP001");
    
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUserDetails);
    when(userRepository.findUserByEmail(authRequest.email())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(RuntimeException.class, () -> 
        userService.authenticateUser(authRequest));
  }

  @Test
  public void testUpdatePassword_WithValidUser_ShouldUpdatePassword() {
    // Arrange
    PasswordChange passwordChange = new PasswordChange("newPassword123");
    String encodedPassword = "encoded.new.password";
    
    mockUser.setPasswords(new ArrayList<>(List.of(getDummyPassword("oldPassword"))));
    
    when(SecurityContextHolder.getContext()).thenReturn(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(mockUserDetails);
    when(userRepository.findUserByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.encode(passwordChange.password())).thenReturn(encodedPassword);
    when(userRepository.save(any(User.class))).thenReturn(mockUser);

    // Act
    userService.updatePassword(passwordChange);

    // Assert
    verify(passwordEncoder).encode(passwordChange.password());
    verify(userRepository).save(argThat(user -> {
      List<Password> passwords = user.getPasswords();
      return passwords.size() == 2 && 
             passwords.get(1).getPassword().equals(encodedPassword);
    }));
  }

  @Test
  public void testUpdatePassword_WithMaxPasswordHistory_ShouldRemoveOldestPassword() {
    // Arrange
    PasswordChange passwordChange = new PasswordChange("newPassword123");
    String encodedPassword = "encoded.new.password";
    
    // Create 12 passwords (max limit)
    List<Password> existingPasswords = new ArrayList<>();
    for (int i = 0; i < 12; i++) {
      existingPasswords.add(getDummyPassword("password" + i));
    }
    mockUser.setPasswords(existingPasswords);
    
    when(SecurityContextHolder.getContext()).thenReturn(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(mockUserDetails);
    when(userRepository.findUserByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.encode(passwordChange.password())).thenReturn(encodedPassword);
    when(userRepository.save(any(User.class))).thenReturn(mockUser);

    // Act
    userService.updatePassword(passwordChange);

    // Assert
    verify(userRepository).save(argThat(user -> {
      List<Password> passwords = user.getPasswords();
      return passwords.size() == 12 && // Still 12 passwords after removing one and adding one
             passwords.get(11).getPassword().equals(encodedPassword); // New password is at the end
    }));
  }

  @Test
  public void testUpdatePassword_WithNoAuthenticatedUser_ShouldThrowException() {
    // Arrange
    PasswordChange passwordChange = new PasswordChange("newPassword123");
    
    when(SecurityContextHolder.getContext()).thenReturn(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        userService.updatePassword(passwordChange));
    
    assertEquals("User not found", exception.getMessage());
  }

  @Test
  public void testUpdatePassword_WithNonExistentUser_ShouldThrowException() {
    // Arrange
    PasswordChange passwordChange = new PasswordChange("newPassword123");
    
    when(SecurityContextHolder.getContext()).thenReturn(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(mockUserDetails);
    when(userRepository.findUserByEmail(mockUser.getEmail())).thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        userService.updatePassword(passwordChange));
    
    assertEquals("User not found", exception.getMessage());
  }

  @Test
  public void testUpdatePassword_WithNullSecurityContext_ShouldThrowException() {
    // Arrange
    PasswordChange passwordChange = new PasswordChange("newPassword123");
    
    when(SecurityContextHolder.getContext()).thenReturn(null);

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> 
        userService.updatePassword(passwordChange));
    
    assertEquals("User not found", exception.getMessage());
  }
} 