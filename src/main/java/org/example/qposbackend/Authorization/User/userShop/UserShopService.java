package org.example.qposbackend.Authorization.User.userShop;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.Roles.SystemRoleRepository;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.UserRepository;
import org.example.qposbackend.Authorization.User.UserService;
import org.example.qposbackend.Authorization.User.userShop.dto.CreateUserShopRequest;
import org.example.qposbackend.Authorization.User.userShop.dto.UpdateUserShopRequest;
import org.example.qposbackend.Authorization.User.userShop.dto.UserShopResponse;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserShopService {
  private final UserShopRepository userShopRepository;
  private final SystemRoleRepository systemRoleRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final SpringSecurityAuditorAware auditorAware;

  public UserShop addUserToShop(User user, Shop shop) {
    SystemRole systemRole = systemRoleRepository.findById("OWNER").orElseThrow();
    return addUserToShop(user, shop, systemRole);
  }

  public UserShop addUserToShop(User user, Shop shop, SystemRole role) {
    UserShop userShop = new UserShop();
    userShop.setUser(user);
    userShop.setShop(shop);
    userShop.setRole(role);

    return userShopRepository.save(userShop);
  }

  @Transactional
  public UserShopResponse createUserShop(CreateUserShopRequest request) {
    UserShop currentUserShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));
    Shop currentShop = currentUserShop.getShop();

    log.info("request is: {}", request);

    User user;
    if (request.getUserId() != null) {
      user =
          userRepository
              .findById(request.getUserId())
              .orElseThrow(() -> new RuntimeException("User not found"));
    } else if (request.getUser() != null) {
      user = userService.createUser(request.getUser());
    } else {
      throw new RuntimeException("User details must be provided");
    }

    SystemRole role =
        systemRoleRepository
            .findById(request.getRoleId())
            .orElseThrow(() -> new RuntimeException("Role not found"));

    if (userShopRepository.existsByUserAndShopAndIsDeletedFalse(user, currentShop)) {
      throw new RuntimeException("User is already associated with this shop");
    }

    List<UserShop> existingUserShops = userShopRepository.findByUserAndIsDeletedFalse(user);
    boolean isOnlyShop = existingUserShops.isEmpty();

    UserShop userShop =
        UserShop.builder()
            .user(user)
            .shop(currentShop)
            .role(role)
            .isDefault(isOnlyShop)
            .isActive(true)
            .isDeleted(false)
            .build();

    UserShop savedUserShop = userShopRepository.save(userShop);
    return convertToResponse(savedUserShop);
  }

  public List<UserShopResponse> getAllUserShopsForCurrentShop() {
    UserShop currentUserShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));
    Shop currentShop = currentUserShop.getShop();

    return userShopRepository.findByShopAndIsDeletedFalseOrderByCreatedAtDesc(currentShop).stream()
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }

  public UserShopResponse getUserShopById(Long id) {
    UserShop currentUserShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));
    Shop currentShop = currentUserShop.getShop();

    UserShop userShop =
        userShopRepository
            .findByIdAndShopAndIsDeletedFalse(id, currentShop)
            .orElseThrow(() -> new RuntimeException("UserShop not found or not accessible"));

    return convertToResponse(userShop);
  }

  @Transactional
  public UserShopResponse updateUserShop(UpdateUserShopRequest request) {
    UserShop currentUserShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));
    Shop currentShop = currentUserShop.getShop();

    UserShop userShop =
        userShopRepository
            .findByIdAndShopAndIsDeletedFalse(request.getId(), currentShop)
            .orElseThrow(() -> new RuntimeException("UserShop not found or not accessible"));

    if (request.getRoleId() != null) {
      SystemRole role =
          systemRoleRepository
              .findById(request.getRoleId())
              .orElseThrow(() -> new RuntimeException("Role not found"));
      userShop.setRole(role);
    }

    if (request.getIsDefault() != null) {
      if (request.getIsDefault()) {
        userShopRepository.findByShopAndIsDeletedFalse(currentShop).stream()
            .filter(
                us ->
                    us.getUser().equals(userShop.getUser())
                        && us.isDefault()
                        && !us.getId().equals(userShop.getId()))
            .forEach(
                us -> {
                  us.setDefault(false);
                  userShopRepository.save(us);
                });
      }
      userShop.setDefault(request.getIsDefault());
    }

    if (request.getIsActive() != null) {
      userShop.setActive(request.getIsActive());
    }

    UserShop savedUserShop = userShopRepository.save(userShop);
    return convertToResponse(savedUserShop);
  }

  @Transactional
  public void lockOrDelete(Long id, boolean isDelete) {
    UserShop currentUserShop =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));
    Shop currentShop = currentUserShop.getShop();

    UserShop userShop =
        userShopRepository
            .findByIdAndShopAndIsDeletedFalse(id, currentShop)
            .orElseThrow(() -> new RuntimeException("UserShop not found or not accessible"));

    if (userShop.getId().equals(currentUserShop.getId())) {
      throw new RuntimeException(
          "Cannot " + (isDelete ? "delete" : "lock") + " your own shop association");
    }

    User affectedUser = userShop.getUser();
    boolean wasDefault = userShop.isDefault();

    if (isDelete) {
      userShop.setDeleted(true);
    }

    userShop.setActive(false);
    userShopRepository.save(userShop);

    List<UserShop> remainingUserShops =
        userShopRepository.findByUserAndIsDeletedFalse(affectedUser);

    if (remainingUserShops.size() == 1) {
      UserShop lastShop = remainingUserShops.getFirst();
      if (wasDefault || remainingUserShops.stream().noneMatch(UserShop::isDefault)) {
        lastShop.setDefault(true);
        userShopRepository.save(lastShop);
      }
    }
  }

  private UserShopResponse convertToResponse(UserShop userShop) {
    UserShopResponse response = new UserShopResponse();
    response.setId(userShop.getId());
    response.setUser(userShop.getUser());
    response.setShop(userShop.getShop());
    response.setRole(userShop.getRole());
    response.setDefault(userShop.isDefault());
    response.setActive(userShop.isActive());
    response.setCreatedAt(userShop.getCreatedAt());
    response.setUpdatedAt(userShop.getUpdatedAt());
    return response;
  }
}
