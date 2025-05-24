package org.example.qposbackend.Authorization.User.userShop;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.Roles.SystemRoleRepository;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserShopService {
  private final UserShopRepository userShopRepository;
  private final SystemRoleRepository systemRoleRepository;

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
}
