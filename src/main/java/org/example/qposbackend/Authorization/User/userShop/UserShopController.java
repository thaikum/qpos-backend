package org.example.qposbackend.Authorization.User.userShop;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.example.qposbackend.Authorization.Privileges.RequirePrivilege;
import org.example.qposbackend.Authorization.User.dto.UserResponse;
import org.example.qposbackend.Authorization.User.userShop.dto.CreateUserShopRequest;
import org.example.qposbackend.Authorization.User.userShop.dto.UpdateUserShopRequest;
import org.example.qposbackend.Authorization.User.userShop.dto.UserShopResponse;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("user-shops")
@RequiredArgsConstructor
@Slf4j
public class UserShopController {

  private final UserShopService userShopService;

  /**
   * Create a new UserShop association for the current shop Only users from the current
   * authenticated shop can be managed
   */
  @PostMapping
  @RequirePrivilege(PrivilegesEnum.ADD_USER)
  public ResponseEntity<DataResponse> createUserShop(
      @Valid @RequestBody CreateUserShopRequest request) {
    try {
      UserShopResponse response = userShopService.createUserShop(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse(response, null));
    } catch (Exception e) {
      log.error("Error creating UserShop: ", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new DataResponse(null, e.getMessage()));
    }
  }

  /**
   * Get all UserShop associations for the current shop Automatically filtered by the authenticated
   * user's shop
   */
  @GetMapping
  @RequirePrivilege(PrivilegesEnum.VIEW_USERS)
  public ResponseEntity<DataResponse> getAllUserShops() {
    try {
      List<UserShopResponse> userShops = userShopService.getAllUserShopsForCurrentShop();
      return ResponseEntity.ok(new DataResponse(userShops, null));
    } catch (Exception e) {
      log.error("Error fetching UserShops: ", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new DataResponse(null, e.getMessage()));
    }
  }

  /**
   * Get all Users for the current shop (just user info, not full UserShop details) Automatically
   * filtered by the authenticated user's shop
   */
  @GetMapping("users-only")
  @RequirePrivilege(PrivilegesEnum.VIEW_USERS)
  public ResponseEntity<DataResponse> getAllUsersPerShop() {
    try {
      List<UserShopResponse> userShops = userShopService.getAllUserShopsForCurrentShop();
      List<UserResponse> userResponses =
          userShops.stream()
              .map(
                  userShop -> {
                    UserResponse userResponse = new UserResponse();
                    userResponse.setEmail(userShop.getUser().getEmail());
                    userResponse.setFirstName(userShop.getUser().getFirstName());
                    userResponse.setLastName(userShop.getUser().getLastName());
                    userResponse.setPhoneNumber(
                        null); // UserResponse extends UserDto but User doesn't have phoneNumber
                    userResponse.setIdType(userShop.getUser().getIdType());
                    userResponse.setIdNumber(userShop.getUser().getIdNumber());
                    userResponse.setSystemRole(userShop.getRole());
                    userResponse.setIsActive(userShop.isActive());
                    userResponse.setId(userShop.getId());
                    return userResponse;
                  })
              .collect(Collectors.toList());
      return ResponseEntity.ok(new DataResponse(userResponses, null));
    } catch (Exception e) {
      log.error("Error fetching users for shop: ", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new DataResponse(null, e.getMessage()));
    }
  }

  /**
   * Get a specific UserShop by ID Only accessible if it belongs to the current authenticated user's
   * shop
   */
  @GetMapping("/{id}")
  @RequirePrivilege(PrivilegesEnum.VIEW_USERS)
  public ResponseEntity<DataResponse> getUserShopById(@PathVariable Long id) {
    try {
      UserShopResponse userShop = userShopService.getUserShopById(id);
      return ResponseEntity.ok(new DataResponse(userShop, null));
    } catch (Exception e) {
      log.error("Error fetching UserShop with id {}: ", id, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new DataResponse(null, e.getMessage()));
    }
  }

  @DeleteMapping("lock-user/{id}")
  @RequirePrivilege(PrivilegesEnum.UPDATE_USER)
  public ResponseEntity<MessageResponse> lockUser(@PathVariable Long id) {
    try {
      userShopService.lockOrDelete(id, false);
      return ResponseEntity.ok(new MessageResponse("User locked for current shop"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new MessageResponse(e.getMessage()));
    }
  }

  /**
   * Update a UserShop association Only accessible if it belongs to the current authenticated user's
   * shop
   */
  @PutMapping
  @RequirePrivilege({
    PrivilegesEnum.ADD_USER,
    PrivilegesEnum.VIEW_USERS
  }) // Example: requires ANY of these privileges
  public ResponseEntity<DataResponse> updateUserShop(
      @Valid @RequestBody UpdateUserShopRequest request) {
    try {
      UserShopResponse response = userShopService.updateUserShop(request);
      return ResponseEntity.ok(new DataResponse(response, null));
    } catch (Exception e) {
      log.error("Error updating UserShop: ", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new DataResponse(null, e.getMessage()));
    }
  }

  /**
   * Delete (soft delete) a UserShop association Only accessible if it belongs to the current
   * authenticated user's shop Cannot delete own association
   */
  @DeleteMapping("/{id}")
  @RequirePrivilege(
      value = {PrivilegesEnum.ADD_USER, PrivilegesEnum.VIEW_USERS},
      requireAll = true) // Example: requires ALL privileges
  public ResponseEntity<MessageResponse> deleteUserShop(@PathVariable Long id) {
    try {
      userShopService.lockOrDelete(id, true);
      return ResponseEntity.ok(new MessageResponse("UserShop association deleted successfully"));
    } catch (Exception e) {
      log.error("Error deleting UserShop with id {}: ", id, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new MessageResponse(e.getMessage()));
    }
  }

  /**
   * Alternative update endpoint using path variable for ID For compatibility with frontend that
   * might prefer this pattern
   */
  @PutMapping("/{id}")
  public ResponseEntity<DataResponse> updateUserShopById(
      @PathVariable Long id, @Valid @RequestBody UpdateUserShopRequest request) {
    try {
      // Set the ID from path variable
      request.setId(id);
      UserShopResponse response = userShopService.updateUserShop(request);
      return ResponseEntity.ok(new DataResponse(response, null));
    } catch (Exception e) {
      log.error("Error updating UserShop with id {}: ", id, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new DataResponse(null, e.getMessage()));
    }
  }
}
