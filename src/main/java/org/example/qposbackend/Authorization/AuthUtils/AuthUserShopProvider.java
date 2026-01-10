package org.example.qposbackend.Authorization.AuthUtils;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class AuthUserShopProvider {
  private final SpringSecurityAuditorAware auditorAware;

  public UserShop getCurrentUserShop() {
    return auditorAware
        .getCurrentAuditor()
        .orElseThrow(() -> new NoSuchElementException("User not found"));
  }

  public User getCurrentUser(){
    return getCurrentUserShop().getUser();
  }

  public Shop getCurrentShop(){
    return getCurrentUserShop().getShop();
  }
}
