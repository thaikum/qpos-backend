package org.example.qposbackend.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.userShop.UserShopService;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.shop.dto.CreateShopInput;
import org.example.qposbackend.shop.dto.UpdateShopInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {
  private final ShopRepository shopRepository;
  private final ObjectMapper objectMapper;
  private final SpringSecurityAuditorAware auditorAware;
  private final UserShopService userShopService;

  public Shop getShop(Long id) {
    return shopRepository.findById(id).orElseThrow(() -> new RuntimeException("Shop not found"));
  }

  public List<Shop> getAllShops() {
    return shopRepository.findAll();
  }

  @Transactional
  public Shop createShop(CreateShopInput input) {
    User user =
        auditorAware
            .getCurrentAuditor()
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getUser();
    Shop shop = objectMapper.convertValue(input, Shop.class);
    shop = shopRepository.save(shop);
    userShopService.addUserToShop(user, shop);
    return shopRepository.save(shop); // to save the shop code
  }

  public Shop updateShop(UpdateShopInput input) {
    Shop shop = getShop(input.getId());
    if (input.getName() != null) shop.setName(input.getName());
    if (input.getPhone() != null) shop.setPhone(input.getPhone());
    if (input.getEmail() != null) shop.setEmail(input.getEmail());
    if (input.getAddress() != null) shop.setAddress(input.getAddress());
    if (input.getLocation() != null) shop.setLocation(input.getLocation());
    if (input.getActive() != null) shop.setActive(input.getActive());
    if (input.getCurrency() != null) shop.setCurrency(input.getCurrency());
    return shopRepository.save(shop);
  }

  @Transactional
  public Shop deleteShop(String shopCode) {
    return shopRepository.deleteShopByCode(shopCode);
  }
}
