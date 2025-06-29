package org.example.qposbackend.data;

import java.util.ArrayList;
import java.util.Calendar;
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
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.ReturnItemRequest;
import org.example.qposbackend.Accounting.Accounts.Account;
import org.example.qposbackend.DTOs.BundledConditionDTO;
import org.example.qposbackend.DTOs.OfferDTO;
import org.example.qposbackend.OffersAndPromotions.Offers.Offer;
import org.example.qposbackend.OffersAndPromotions.BundledConditions.BundledCondition;
import org.example.qposbackend.OffersAndPromotions.DiscountType;
import org.example.qposbackend.OffersAndPromotions.OfferBasedOn;
import org.example.qposbackend.OffersAndPromotions.OfferOn;
import org.example.qposbackend.OffersAndPromotions.OfferType;
import org.example.qposbackend.OffersAndPromotions.AppliedOffersAndTotalDiscount;
import org.example.qposbackend.OffersAndPromotions.OrderWithDiscountsAndAppliedOffers;
import org.example.qposbackend.Item.ItemClassification.Category.Category;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategory;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategory;

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

  public static DateRange getDummyDateRange() {
    Calendar cal = Calendar.getInstance();
    Date endDate = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, -30);
    Date startDate = cal.getTime();
    return new DateRange(startDate, endDate);
  }

  public static ReturnItemRequest getDummyReturnItemRequest() {
    return new ReturnItemRequest(1L, "Defective item", 1, 0.0);
  }

  public static Account getDummyAccount() {
    return getDummyAccount("CASH");
  }

  public static Account getDummyAccount(String accountName) {
    Account account = new Account();
    account.setId(1L);
    account.setAccountName(accountName);
    account.setBalance(1000.0);
    account.setAccountNumber("ACC001");
    return account;
  }

  public static Offer getDummyOffer() {
    return getDummyOffer("Test Offer");
  }

  public static Offer getDummyOffer(String offerName) {
    Offer offer = new Offer();
    offer.setId(1L);
    offer.setOfferName(offerName);
    offer.setDescription("Test offer description");
    offer.setEffectOn(OfferOn.ITEMS);
    offer.setBasedOn(OfferBasedOn.QUANTITY);
    offer.setDiscountType(DiscountType.PERCENTAGE);
    offer.setDiscountAllowed(10.0);
    offer.setMinQuantity(2);
    offer.setActive(true);
    offer.setApplyMultipleOnSameOrder(false);
    offer.setMaxDiscountPerOrder(50.0);
    return offer;
  }

  public static OfferDTO getDummyOfferDTO() {
    return getDummyOfferDTO("Test Offer DTO");
  }

  public static OfferDTO getDummyOfferDTO(String offerName) {
    OfferDTO offerDTO = new OfferDTO();
    offerDTO.setOfferName(offerName);
    offerDTO.setDescription("Test offer DTO description");
    offerDTO.setOfferType(OfferType.ITEM_BASED);
    offerDTO.setDiscountType(DiscountType.PERCENTAGE);
    offerDTO.setDiscountAllowed(15.0);
    offerDTO.setEffectOn(OfferOn.ITEMS);
    offerDTO.setBasedOn(OfferBasedOn.ITEMS);
    offerDTO.setAffectedIds(List.of(1L, 2L));
    offerDTO.setActive(true);
    offerDTO.setApplyMultipleOnSameOrder(false);
    offerDTO.setMaxDiscountPerOrder(100.0);
    return offerDTO;
  }

  public static BundledConditionDTO getDummyBundledConditionDTO() {
    BundledConditionDTO dto = new BundledConditionDTO();
    dto.setValueId(1L);
    dto.setMinQuantity(2);
    dto.setMinAmount(50.0);
    return dto;
  }

  public static BundledCondition getDummyBundledCondition() {
    BundledCondition condition = new BundledCondition();
    condition.setId(1L);
    condition.setMinQuantity(2);
    condition.setMinAmount(50.0);
    return condition;
  }

  public static Category getDummyCategory() {
    return getDummyCategory("Test Category");
  }

  public static Category getDummyCategory(String categoryName) {
    Category category = new Category();
    category.setId(1L);
    category.setCategoryName(categoryName);
    category.setMainCategory(getDummyMainCategory());
    return category;
  }

  public static MainCategory getDummyMainCategory() {
    return getDummyMainCategory("Test Main Category");
  }

  public static MainCategory getDummyMainCategory(String mainCategoryName) {
    MainCategory mainCategory = new MainCategory();
    mainCategory.setId(1L);
    mainCategory.setMainCategoryName(mainCategoryName);
    return mainCategory;
  }

  public static SubCategory getDummySubCategory() {
    return getDummySubCategory("Test Sub Category");
  }

  public static SubCategory getDummySubCategory(String subCategoryName) {
    SubCategory subCategory = new SubCategory();
    subCategory.setId(1L);
    subCategory.setSubCategoryName(subCategoryName);
    subCategory.setCategory(getDummyCategory());
    return subCategory;
  }

  public static AppliedOffersAndTotalDiscount getDummyAppliedOffersAndTotalDiscount() {
    return new AppliedOffersAndTotalDiscount(
        getDummyOffer(), 
        25.0, 
        List.of(getDummyOrderItem())
    );
  }

  public static OrderWithDiscountsAndAppliedOffers getDummyOrderWithDiscountsAndAppliedOffers() {
    return new OrderWithDiscountsAndAppliedOffers(
        getDummySaleOrder(),
        List.of(getDummyAppliedOffersAndTotalDiscount())
    );
  }
}
