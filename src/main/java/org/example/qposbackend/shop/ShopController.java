package org.example.qposbackend.shop;

import org.example.qposbackend.shop.dto.CreateShopInput;
import org.example.qposbackend.shop.dto.UpdateShopInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ShopController {

  private final ShopService shopService;

  public ShopController(ShopService shopService) {
    this.shopService = shopService;
  }

  @QueryMapping
  public Shop shop(@Argument Long id) {
    return shopService.getShop(id);
  }

  @QueryMapping
  public List<Shop> shops() {
    return shopService.getAllShops();
  }

  @MutationMapping
  public Shop createShop(@Argument CreateShopInput input) {
    return shopService.createShop(input);
  }

  @MutationMapping
  public Shop updateShop(@Argument UpdateShopInput input) {
    return shopService.updateShop(input);
  }

  @MutationMapping
  public Shop deleteShop(@Argument String shopCode) {
    return shopService.deleteShop(shopCode);
  }
}
