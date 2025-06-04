package org.example.qposbackend.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.User.IdType;
import org.example.qposbackend.Authorization.User.Password.Password;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.UserActivity.UserActivity;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryStatus;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;
import org.example.qposbackend.InventoryItem.PriceDetails.PriceDetails;
import org.example.qposbackend.InventoryItem.PriceDetails.PricingMode;
import org.example.qposbackend.Item.Item;
import org.example.qposbackend.Item.UnitsOfMeasure;
import org.example.qposbackend.Utils.StoqItUtils;
import org.example.qposbackend.order.SaleOrder;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.order.orderItem.ReturnInward.ReturnInward;
import org.example.qposbackend.shop.Shop;

public class DummyData {
  public static Password getDummyPassword() {
    return getDummyPassword("password1");
  }

  public static Password getDummyPassword(String password) {
    return Password.builder().password(password).build();
  }

  public static User getDummyUser() {
    Password password1 = getDummyPassword("password1");
    Password password2 = getDummyPassword("password2");
    return User.builder()
        .firstName("Alice")
        .lastName("Smith")
        .email("alice.smith@example.com")
        .idType(IdType.NATIONAL_ID)
        .idNumber("123456789")
        .passwords(List.of(password1, password2))
        .enabled(true)
        .isLoggedIn(false)
        .build();
  }

  public static Shop getDummyShop() {
    return Shop.builder()
        .id(42L)
        .name("Sunshine Electronics")
        .phone("+254712345678")
        .email("sunshine@example.com")
        .address("123 Market Street, Nairobi")
        .location("Nairobi CBD")
        .currency("KES")
        .code(StoqItUtils.generateStringFromLong(43L, 9))
        .active(true)
        .deleted(false)
        .createdAt(new Date())
        .updatedAt(new Date())
        .build();
  }

  public static UserShop getDummyUserShop() {
    return UserShop.builder().user(getDummyUser()).shop(getDummyShop()).build();
  }

  public static Item getDummyItem(String itemName) {
    return Item.builder()
        .barCode("ABC123XYZ")
        .name(itemName)
        .mainCategory("Electronics")
        .category("Accessories")
        .subCategory("Computer Peripherals")
        .brand("LogiTech")
        .color("Black")
        .buyingPrice(500.0)
        .minSellingPrice(700.0)
        .maxSellingPrice(1000.0)
        .unitOfMeasure(UnitsOfMeasure.PIECES)
        .minimumPerUnit(1.0)
        .description("Ergonomic wireless mouse with USB receiver.")
        .build();
  }

  public static Item getDummyItem() {
    return getDummyItem("Item 1");
  }

  public static SystemRole getDummySystemRole() {
    return SystemRole.builder().name("ADMIN").build();
  }

  public static UserActivity getDummyUserActivity() {
    return UserActivity.builder().userShop(getDummyUserShop()).build();
  }

  public static ReturnInward getDummyReturnInward() {
    return ReturnInward.builder()
        .quantityReturned(1)
        .returnReason("Spoilt item")
        .dateSold(new Date())
        .dateReturned(new Date())
        .build();
  }

  public static Price getDummyPrice() {
    return Price.builder()
        .buyingPrice(100.0)
        .sellingPrice(150.0)
        .quantityUnderThisPrice(50)
        .discountAllowed(10.0)
        .status(PriceStatus.ACTIVE)
        .build();
  }

  public static PriceDetails getDummyPriceDetails() {
    return PriceDetails.builder()
        .pricingMode(PricingMode.FIXED_PROFIT)
        .profitPercentage(20.0)
        .fixedProfit(50.0)
        .prices(List.of(getDummyPrice()))
        .build();
  }

  public static InventoryItem getDummyInventoryItem() {
    return getDummyInventoryItem("Item 1");
  }

  public static InventoryItem getDummyInventoryItem(String itemName) {
    return InventoryItem.builder()
        .item(getDummyItem(itemName))
        .inventoryStatus(InventoryStatus.AVAILABLE)
        .reorderLevel(10)
        .priceDetails(getDummyPriceDetails())
        .shop(getDummyShop())
        .build();
  }

  public static OrderItem getDummyOrderItem() {
    return getDummyOrderItem("Item 1");
  }

  public static OrderItem getDummyOrderItem(String itemName) {
    return OrderItem.builder()
        .inventoryItem(getDummyInventoryItem(itemName))
        .quantity(3)
        .price(150.0)
        .buyingPrice(100.0)
        .discount(5.0)
        .discountMode("Amount")
        .offersApplied(new ArrayList<>())
        .build();
  }

  public static SaleOrder getDummySaleOrder() {
    return SaleOrder.builder()
        .orderItems(List.of(getDummyOrderItem()))
        .discount(5.0)
        .modeOfPayment("Mpesa")
        .amountInMpesa(450.0)
        .amountInCash(0.0)
        .amountInCredit(0.0)
        .shop(getDummyShop())
        .build();
  }
}
