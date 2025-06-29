package org.example.qposbackend.OffersAndPromotions.Offers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.qposbackend.DTOs.BundledConditionDTO;
import org.example.qposbackend.DTOs.OfferDTO;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.Item.ItemClassification.Category.Category;
import org.example.qposbackend.Item.ItemClassification.Category.CategoryRepository;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategory;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategoryRepository;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategory;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategoryRepository;
import org.example.qposbackend.OffersAndPromotions.BundledConditions.BundledCondition;
import org.example.qposbackend.OffersAndPromotions.DiscountType;
import org.example.qposbackend.OffersAndPromotions.OfferBasedOn;
import org.example.qposbackend.OffersAndPromotions.OfferOn;
import org.example.qposbackend.OffersAndPromotions.OrderWithDiscountsAndAppliedOffers;
import org.example.qposbackend.order.SaleOrder;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfferServiceTest {
  @Mock private OfferRepository offerRepository;
  @Mock private ObjectMapper objectMapper;
  @Mock private InventoryItemRepository inventoryItemRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private MainCategoryRepository mainCategoryRepository;
  @Mock private SubCategoryRepository subCategoryRepository;
  @InjectMocks private OfferService offerService;

  private Offer mockOffer;
  private OfferDTO mockOfferDTO;
  private SaleOrder mockSaleOrder;
  private InventoryItem mockInventoryItem;

  @BeforeEach
  public void setUp() {
    mockOffer = getDummyOffer();
    mockOfferDTO = getDummyOfferDTO();
    mockSaleOrder = getDummySaleOrder();
    mockInventoryItem = getDummyInventoryItem();
    
    // Set up inventory item hierarchy for testing
    SubCategory subCategory = getDummySubCategory();
    Category category = getDummyCategory();
    MainCategory mainCategory = getDummyMainCategory();
    
    subCategory.setCategory(category);
    category.setMainCategory(mainCategory);
    mockInventoryItem.getItem().setSubCategoryId(subCategory);
  }

  @Test
  public void testCreateOffer_WithItemBasedOffer_ShouldCreateOfferWithItems() {
    // Arrange
    mockOfferDTO.setEffectOn(OfferOn.ITEMS);
    mockOfferDTO.setBasedOn(OfferBasedOn.ITEMS);
    mockOfferDTO.setAffectedIds(List.of(1L, 2L));
    mockOfferDTO.setBundledConditions(List.of(getDummyBundledConditionDTO()));
    
    List<InventoryItem> affectedItems = List.of(mockInventoryItem, getDummyInventoryItem("Item 2"));
    
    when(objectMapper.convertValue(mockOfferDTO, Offer.class)).thenReturn(mockOffer);
    when(inventoryItemRepository.findAllById(mockOfferDTO.getAffectedIds()))
        .thenReturn(affectedItems);
    when(objectMapper.convertValue(any(BundledConditionDTO.class), eq(BundledCondition.class)))
        .thenReturn(getDummyBundledCondition());
    when(inventoryItemRepository.getReferenceById(anyLong())).thenReturn(mockInventoryItem);
    when(offerRepository.save(any(Offer.class))).thenReturn(mockOffer);

    // Act
    offerService.createOffer(mockOfferDTO);

    // Assert
    verify(objectMapper).convertValue(mockOfferDTO, Offer.class);
    verify(inventoryItemRepository).findAllById(mockOfferDTO.getAffectedIds());
    verify(offerRepository).save(argThat(offer -> 
        offer.getItems() != null && offer.getItems().size() == 2));
  }

  @Test
  public void testCreateOffer_WithCategoryBasedOffer_ShouldCreateOfferWithCategories() {
    // Arrange
    mockOfferDTO.setEffectOn(OfferOn.CATEGORIES);
    mockOfferDTO.setAffectedIds(List.of(1L, 2L));
    mockOfferDTO.setBundledConditions(List.of(getDummyBundledConditionDTO()));
    
    List<Category> affectedCategories = List.of(getDummyCategory(), getDummyCategory("Category 2"));
    
    when(objectMapper.convertValue(mockOfferDTO, Offer.class)).thenReturn(mockOffer);
    when(categoryRepository.findAllById(mockOfferDTO.getAffectedIds()))
        .thenReturn(affectedCategories);
    when(objectMapper.convertValue(any(BundledConditionDTO.class), eq(BundledCondition.class)))
        .thenReturn(getDummyBundledCondition());
    when(categoryRepository.getReferenceById(anyLong())).thenReturn(getDummyCategory());
    when(offerRepository.save(any(Offer.class))).thenReturn(mockOffer);

    // Act
    offerService.createOffer(mockOfferDTO);

    // Assert
    verify(categoryRepository).findAllById(mockOfferDTO.getAffectedIds());
    verify(offerRepository).save(argThat(offer -> 
        offer.getCategories() != null && offer.getCategories().size() == 2));
  }

  @Test
  public void testGetOffersToApply_WithNoActiveOffers_ShouldReturnOriginalOrder() {
    // Arrange
    OrderItem orderItem = getDummyOrderItem();
    orderItem.getInventoryItem().setId(1L);
    mockSaleOrder.setOrderItems(List.of(orderItem));
    
    when(inventoryItemRepository.getReferenceById(1L)).thenReturn(mockInventoryItem);
    when(offerRepository.findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(new ArrayList<>());
    when(offerRepository.findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInMainCategoriesIdsAndNotInSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(new ArrayList<>());

    // Act
    OrderWithDiscountsAndAppliedOffers result = offerService.getOffersToApply(mockSaleOrder);

    // Assert
    assertNotNull(result);
    assertEquals(mockSaleOrder, result.saleOrder());
    assertNull(result.appliedOffersAndTotalDiscounts());
  }

  @Test
  public void testCreateOffer_WithNullBundledConditions_ShouldNotSaveOffer() {
    // Arrange
    mockOfferDTO.setBundledConditions(null);
    
    when(objectMapper.convertValue(mockOfferDTO, Offer.class)).thenReturn(mockOffer);

    // Act
    offerService.createOffer(mockOfferDTO);

    // Assert
    verify(offerRepository, never()).save(any(Offer.class));
  }

  @Test
  public void testCreateOffer_WithMainCategoryBasedOffer_ShouldCreateOfferWithMainCategories() {
    // Arrange
    mockOfferDTO.setEffectOn(OfferOn.MAIN_CATEGORIES);
    mockOfferDTO.setAffectedIds(List.of(1L, 2L));
    
    List<MainCategory> affectedMainCategories = List.of(getDummyMainCategory(), getDummyMainCategory("Main Category 2"));
    
    when(objectMapper.convertValue(mockOfferDTO, Offer.class)).thenReturn(mockOffer);
    when(mainCategoryRepository.findAllById(mockOfferDTO.getAffectedIds()))
        .thenReturn(affectedMainCategories);
    when(offerRepository.save(any(Offer.class))).thenReturn(mockOffer);

    // Act
    offerService.createOffer(mockOfferDTO);

    // Assert
    verify(mainCategoryRepository).findAllById(mockOfferDTO.getAffectedIds());
    verify(offerRepository).save(argThat(offer -> 
        offer.getMainCategories() != null && offer.getMainCategories().size() == 2));
  }

  @Test
  public void testCreateOffer_WithSubCategoryBasedOffer_ShouldCreateOfferWithSubCategories() {
    // Arrange
    mockOfferDTO.setEffectOn(OfferOn.SUB_CATEGORIES);
    mockOfferDTO.setAffectedIds(List.of(1L, 2L));
    
    List<SubCategory> affectedSubCategories = List.of(getDummySubCategory(), getDummySubCategory("Sub Category 2"));
    
    when(objectMapper.convertValue(mockOfferDTO, Offer.class)).thenReturn(mockOffer);
    when(subCategoryRepository.findAllById(mockOfferDTO.getAffectedIds()))
        .thenReturn(affectedSubCategories);
    when(offerRepository.save(any(Offer.class))).thenReturn(mockOffer);

    // Act
    offerService.createOffer(mockOfferDTO);

    // Assert
    verify(subCategoryRepository).findAllById(mockOfferDTO.getAffectedIds());
    verify(offerRepository).save(argThat(offer -> 
        offer.getSubCategories() != null && offer.getSubCategories().size() == 2));
  }

  @Test
  public void testCreateOffer_WithBundledConditions_ShouldCreateOfferWithBundledConditions() {
    // Arrange
    BundledConditionDTO bundledConditionDTO = getDummyBundledConditionDTO();
    mockOfferDTO.setBundledConditions(List.of(bundledConditionDTO));
    mockOfferDTO.setBasedOn(OfferBasedOn.ITEMS);
    
    BundledCondition bundledCondition = getDummyBundledCondition();
    
    when(objectMapper.convertValue(mockOfferDTO, Offer.class)).thenReturn(mockOffer);
    when(objectMapper.convertValue(bundledConditionDTO, BundledCondition.class))
        .thenReturn(bundledCondition);
    when(inventoryItemRepository.getReferenceById(bundledConditionDTO.getValueId()))
        .thenReturn(mockInventoryItem);
    when(offerRepository.save(any(Offer.class))).thenReturn(mockOffer);

    // Act
    offerService.createOffer(mockOfferDTO);

    // Assert
    verify(objectMapper).convertValue(bundledConditionDTO, BundledCondition.class);
    verify(inventoryItemRepository).getReferenceById(bundledConditionDTO.getValueId());
    verify(offerRepository).save(argThat(offer -> 
        offer.getBundledConditions() != null && offer.getBundledConditions().size() == 1));
  }

  @Test
  public void testGetOffersToApply_WithItemBasedOffer_ShouldApplyDiscountToMatchingItems() {
    // Arrange
    OrderItem orderItem = getDummyOrderItem();
    orderItem.getInventoryItem().setId(1L);
    orderItem.setPrice(100.0);
    orderItem.setQuantity(3);
    mockSaleOrder.setOrderItems(List.of(orderItem));
    
    Offer itemBasedOffer = getDummyOffer();
    itemBasedOffer.setEffectOn(OfferOn.ITEMS);
    itemBasedOffer.setBasedOn(OfferBasedOn.QUANTITY);
    itemBasedOffer.setMinQuantity(2);
    itemBasedOffer.setDiscountType(DiscountType.PERCENTAGE);
    itemBasedOffer.setDiscountAllowed(10.0);
    itemBasedOffer.setItems(List.of(mockInventoryItem));
    
    when(inventoryItemRepository.getReferenceById(1L)).thenReturn(mockInventoryItem);
    when(offerRepository.findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(List.of(itemBasedOffer));
    when(offerRepository.findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInMainCategoriesIdsAndNotInSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(new ArrayList<>());

    // Act
    OrderWithDiscountsAndAppliedOffers result = offerService.getOffersToApply(mockSaleOrder);

    // Assert
    assertNotNull(result);
    assertNotNull(result.appliedOffersAndTotalDiscounts());
    assertFalse(result.appliedOffersAndTotalDiscounts().isEmpty());
  }

  @Test
  public void testGetOffersToApply_WithCategoryBasedOffer_ShouldApplyDiscountToMatchingCategories() {
    // Arrange
    OrderItem orderItem = getDummyOrderItem();
    orderItem.getInventoryItem().setId(1L);
    orderItem.setPrice(100.0);
    orderItem.setQuantity(3);
    mockSaleOrder.setOrderItems(List.of(orderItem));
    
    Offer categoryBasedOffer = getDummyOffer();
    categoryBasedOffer.setEffectOn(OfferOn.CATEGORIES);
    categoryBasedOffer.setBasedOn(OfferBasedOn.QUANTITY);
    categoryBasedOffer.setMinQuantity(2);
    categoryBasedOffer.setDiscountType(DiscountType.ABSOLUTE);
    categoryBasedOffer.setDiscountAllowed(15.0);
    categoryBasedOffer.setCategories(List.of(getDummyCategory()));
    
    when(inventoryItemRepository.getReferenceById(1L)).thenReturn(mockInventoryItem);
    when(offerRepository.findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(List.of(categoryBasedOffer));
    when(offerRepository.findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInMainCategoriesIdsAndNotInSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(new ArrayList<>());

    // Act
    OrderWithDiscountsAndAppliedOffers result = offerService.getOffersToApply(mockSaleOrder);

    // Assert
    assertNotNull(result);
    assertNotNull(result.appliedOffersAndTotalDiscounts());
    assertFalse(result.appliedOffersAndTotalDiscounts().isEmpty());
  }

  @Test
  public void testGetOffersToApply_WithAmountBasedOffer_ShouldApplyWhenAmountThresholdMet() {
    // Arrange
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setPrice(100.0);
    orderItem.setQuantity(1);
    orderItem.setDiscount(0.0);
    mockSaleOrder.setOrderItems(List.of(orderItem));
    
    Offer amountBasedOffer = getDummyOffer();
    amountBasedOffer.setEffectOn(OfferOn.ALL);
    amountBasedOffer.setBasedOn(OfferBasedOn.AMOUNT_SPENT);
    amountBasedOffer.setMinAmount(50.0);
    amountBasedOffer.setDiscountType(DiscountType.PERCENTAGE);
    amountBasedOffer.setDiscountAllowed(5.0);
    
    when(inventoryItemRepository.getReferenceById(anyLong())).thenReturn(mockInventoryItem);
    when(offerRepository.findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(List.of(amountBasedOffer));
    when(offerRepository.findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInMainCategoriesIdsAndNotInSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(new ArrayList<>());

    // Act
    OrderWithDiscountsAndAppliedOffers result = offerService.getOffersToApply(mockSaleOrder);

    // Assert
    assertNotNull(result);
    assertNotNull(result.appliedOffersAndTotalDiscounts());
    assertFalse(result.appliedOffersAndTotalDiscounts().isEmpty());
  }

  @Test
  public void testGetOffersToApply_WithQuantityBasedOffer_ShouldApplyWhenQuantityThresholdMet() {
    // Arrange
    OrderItem orderItem1 = getDummyOrderItem();
    orderItem1.setQuantity(2);
    OrderItem orderItem2 = getDummyOrderItem("Item 2");
    orderItem2.setQuantity(1);
    mockSaleOrder.setOrderItems(List.of(orderItem1, orderItem2));
    
    Offer quantityBasedOffer = getDummyOffer();
    quantityBasedOffer.setEffectOn(OfferOn.ALL);
    quantityBasedOffer.setBasedOn(OfferBasedOn.QUANTITY);
    quantityBasedOffer.setMinQuantity(2);
    quantityBasedOffer.setDiscountType(DiscountType.ABSOLUTE);
    quantityBasedOffer.setDiscountAllowed(20.0);
    
    when(inventoryItemRepository.getReferenceById(anyLong())).thenReturn(mockInventoryItem);
    when(offerRepository.findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(List.of(quantityBasedOffer));
    when(offerRepository.findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(new ArrayList<>());

    // Act
    OrderWithDiscountsAndAppliedOffers result = offerService.getOffersToApply(mockSaleOrder);

    // Assert
    assertNotNull(result);
    assertNotNull(result.appliedOffersAndTotalDiscounts());
    assertFalse(result.appliedOffersAndTotalDiscounts().isEmpty());
  }

  @Test
  public void testGetOffersToApply_WithMaxDiscountPerOrder_ShouldLimitDiscount() {
    // Arrange
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setPrice(200.0);
    orderItem.setQuantity(3);
    mockSaleOrder.setOrderItems(List.of(orderItem));
    
    Offer offerWithMaxDiscount = getDummyOffer();
    offerWithMaxDiscount.setEffectOn(OfferOn.ALL);
    offerWithMaxDiscount.setBasedOn(OfferBasedOn.QUANTITY);
    offerWithMaxDiscount.setMinQuantity(1);
    offerWithMaxDiscount.setDiscountType(DiscountType.PERCENTAGE);
    offerWithMaxDiscount.setDiscountAllowed(50.0); // Would be 100 per item
    offerWithMaxDiscount.setMaxDiscountPerOrder(150.0); // Limited to 150
    
    when(inventoryItemRepository.getReferenceById(anyLong())).thenReturn(mockInventoryItem);
    when(offerRepository.findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(List.of(offerWithMaxDiscount));
    when(offerRepository.findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(new ArrayList<>());

    // Act
    OrderWithDiscountsAndAppliedOffers result = offerService.getOffersToApply(mockSaleOrder);

    // Assert
    assertNotNull(result);
    assertNotNull(result.appliedOffersAndTotalDiscounts());
    // Discount should be limited to max discount per order
    assertTrue(result.appliedOffersAndTotalDiscounts().get(0).totalDiscount() <= 150.0);
  }

  @Test
  public void testGetOffersToApply_WithNonApplyMultipleOnSameOrder_ShouldApplyOnlyOnce() {
    // Arrange
    OrderItem orderItem1 = getDummyOrderItem();
    orderItem1.setPrice(100.0);
    OrderItem orderItem2 = getDummyOrderItem("Item 2");
    orderItem2.setPrice(100.0);
    mockSaleOrder.setOrderItems(List.of(orderItem1, orderItem2));
    
    Offer singleApplicationOffer = getDummyOffer();
    singleApplicationOffer.setEffectOn(OfferOn.ALL);
    singleApplicationOffer.setBasedOn(OfferBasedOn.QUANTITY);
    singleApplicationOffer.setMinQuantity(1);
    singleApplicationOffer.setDiscountType(DiscountType.ABSOLUTE);
    singleApplicationOffer.setDiscountAllowed(10.0);
    singleApplicationOffer.setApplyMultipleOnSameOrder(false);
    
    when(inventoryItemRepository.getReferenceById(anyLong())).thenReturn(mockInventoryItem);
    when(offerRepository.findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(List.of(singleApplicationOffer));
    when(offerRepository.findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInSubCategoriesIds(
        any(), any(), any(), any())).thenReturn(new ArrayList<>());

    // Act
    OrderWithDiscountsAndAppliedOffers result = offerService.getOffersToApply(mockSaleOrder);

    // Assert
    assertNotNull(result);
    assertNotNull(result.appliedOffersAndTotalDiscounts());
    // Should only apply discount once, not on both items
    assertEquals(10.0, result.appliedOffersAndTotalDiscounts().get(0).totalDiscount());
  }
} 