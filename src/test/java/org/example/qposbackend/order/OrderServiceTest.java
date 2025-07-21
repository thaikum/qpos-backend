package org.example.qposbackend.order;

import org.assertj.core.util.Streams;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.DTOs.DateRange;
import org.example.qposbackend.DTOs.ReturnItemRequest;
import org.example.qposbackend.EOD.EODRepository;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.example.qposbackend.InventoryItem.InventoryItem;
import org.example.qposbackend.InventoryItem.InventoryItemRepository;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceRepository;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.PriceStatus;
import org.example.qposbackend.OffersAndPromotions.Offers.OfferService;
import org.example.qposbackend.OffersAndPromotions.OrderWithDiscountsAndAppliedOffers;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.example.qposbackend.order.orderItem.OrderItem;
import org.example.qposbackend.order.orderItem.OrderItemRepository;
import org.example.qposbackend.order.orderItem.ReturnInward.ReturnInward;
import org.example.qposbackend.order.orderItem.ReturnInward.ReturnInwardRepository;
import org.example.qposbackend.shop.Shop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private InventoryItemRepository inventoryItemRepository;
  @Mock
  private AccountRepository accountRepository;
  @Mock
  private TranHeaderService tranHeaderService;
  @Mock
  private SpringSecurityAuditorAware auditorAware;
  @Mock
  private OrderItemRepository orderItemRepository;
  @Mock
  private ReturnInwardRepository returnInwardRepository;
  @Mock
  private EODRepository eodRepository;
  @Mock
  private OfferService offerService;
  @Mock
  private PriceRepository priceRepository;
  @InjectMocks
  private OrderService orderService;
  private OrderService orderServiceSpy;

  @BeforeEach
  public void setUp() {
    this.orderServiceSpy = spy(orderService);
    when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(getDummyUserShop()));
  }

  @Test
  public void testProcessOrder_WhenQuantityIs0_ShouldThrowError() {
    InventoryItem inventoryItem = getDummyInventoryItem();
    inventoryItem
        .getPriceDetails()
        .setPrices(
            inventoryItem.getPriceDetails().getPrices().stream()
                .peek(price -> price.setQuantityUnderThisPrice(0))
                .toList());
    inventoryItem.setId(1L);
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setDiscount(0.0);
    orderItem.setInventoryItem(inventoryItem);
    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setOrderItems(List.of(orderItem));
    when(inventoryItemRepository.findById(anyLong())).thenReturn(Optional.of(inventoryItem));
    when(offerService.getOffersToApply(any()))
        .thenReturn(new OrderWithDiscountsAndAppliedOffers(saleOrder, new ArrayList<>()));
    assertThrowsExactly(
        RuntimeException.class, () -> orderService.processOrder(saleOrder), "No enough stock");
  }

  @Test
  public void testProcessOrder_WhenQuantityIsInAnOlderPrice_ShouldDeductFromOldPrice() {
    InventoryItem inventoryItem = getDummyInventoryItem();
    Price price = getDummyPrice();
    Price price1 = getDummyPrice();
    price.setQuantityUnderThisPrice(4);
    price.setStatus(PriceStatus.STOPPED);
    price.setId(3);
    price1.setStatus(PriceStatus.ACTIVE);
    price1.setId(1);
    price1.setQuantityUnderThisPrice(0);
    inventoryItem.getPriceDetails().setPrices(List.of(price, price1));
    inventoryItem.setId(1L);
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setDiscount(0.0);
    orderItem.setQuantity(2);
    orderItem.setInventoryItem(inventoryItem);
    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setOrderItems(List.of(orderItem));
    when(inventoryItemRepository.findById(anyLong())).thenReturn(Optional.of(inventoryItem));
    when(offerService.getOffersToApply(any()))
        .thenReturn(new OrderWithDiscountsAndAppliedOffers(saleOrder, new ArrayList<>()));
    when(inventoryItemRepository.save(any())).thenAnswer(arg -> arg.getArgument(0));
    when(priceRepository.saveAll(
        argThat(
            (args -> {
              Optional<Price> priceOptional = Streams.stream(args).filter(p -> p.getId() == 3).findAny();
              if (priceOptional.isEmpty()) {
                return false;
              }
              Price effectedPrice = priceOptional.get();
              return effectedPrice.getQuantityUnderThisPrice() == 2;
            }))))
        .thenAnswer(arg -> arg.getArgument(0));
    doNothing().when(tranHeaderService).saveAndVerifyTranHeader(any(TranHeader.class));
    when(orderRepository.save(any())).thenAnswer(arg -> arg.getArgument(0));
    doReturn(new TranHeader()).when(orderServiceSpy).makeSale(any());
    orderServiceSpy.processOrder(saleOrder);
  }

  @Test
  public void testProcessOrder_WhenQuantitySpillOverDifferentPrices_ShouldDeductFromEach() {
    InventoryItem inventoryItem = getDummyInventoryItem();
    Price price = getDummyPrice();
    Price price1 = getDummyPrice();
    price.setQuantityUnderThisPrice(2);
    price.setStatus(PriceStatus.STOPPED);
    price.setId(1);
    price1.setStatus(PriceStatus.ACTIVE);
    price1.setId(2);
    price1.setQuantityUnderThisPrice(2);
    inventoryItem.getPriceDetails().setPrices(List.of(price, price1));
    inventoryItem.setId(1L);
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setDiscount(0.0);
    orderItem.setQuantity(3);
    orderItem.setInventoryItem(inventoryItem);
    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setOrderItems(List.of(orderItem));
    when(inventoryItemRepository.findById(anyLong())).thenReturn(Optional.of(inventoryItem));
    when(offerService.getOffersToApply(any()))
        .thenReturn(new OrderWithDiscountsAndAppliedOffers(saleOrder, new ArrayList<>()));
    when(inventoryItemRepository.save(any())).thenAnswer(arg -> arg.getArgument(0));
    when(priceRepository.saveAll(
        argThat(
            (args -> {
              Optional<Price> priceOptional = Streams.stream(args).filter(p -> p.getId() == 1).findAny();
              if (priceOptional.isEmpty()) {
                return false;
              }
              Price effectedPrice = priceOptional.get();

              if (effectedPrice.getQuantityUnderThisPrice() != 0) {
                return false;
              }
              priceOptional = Streams.stream(args).filter(p -> p.getId() == 2).findAny();
              if (priceOptional.isEmpty()) {
                return false;
              }
              effectedPrice = priceOptional.get();
              return effectedPrice.getQuantityUnderThisPrice() == 1;
            }))))
        .thenAnswer(arg -> arg.getArgument(0));
    doNothing().when(tranHeaderService).saveAndVerifyTranHeader(any(TranHeader.class));
    when(orderRepository.save(any())).thenAnswer(arg -> arg.getArgument(0));
    doReturn(new TranHeader()).when(orderServiceSpy).makeSale(any());
    orderServiceSpy.processOrder(saleOrder);
  }

  @Test
  public void testProcessOrder_WhenActivePriceIsNotTheLast_SellingPriceShouldBeOfTheActivePrice() {
    // Arrange
    InventoryItem inventoryItem = getDummyInventoryItem();
    Price activePrice = getDummyPrice();
    Price inactivePrice = getDummyPrice();

    activePrice.setSellingPrice(200.0);
    activePrice.setStatus(PriceStatus.ACTIVE);
    activePrice.setQuantityUnderThisPrice(5);
    activePrice.setId(1);

    inactivePrice.setSellingPrice(250.0);
    inactivePrice.setStatus(PriceStatus.STOPPED);
    inactivePrice.setQuantityUnderThisPrice(0);
    inactivePrice.setId(2);

    inventoryItem.getPriceDetails().setPrices(List.of(activePrice, inactivePrice));
    inventoryItem.setId(1L);

    OrderItem orderItem = getDummyOrderItem();
    orderItem.setDiscount(0.0);
    orderItem.setQuantity(2);
    orderItem.setInventoryItem(inventoryItem);

    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setOrderItems(List.of(orderItem));

    when(inventoryItemRepository.findById(anyLong())).thenReturn(Optional.of(inventoryItem));
    when(offerService.getOffersToApply(any()))
        .thenReturn(new OrderWithDiscountsAndAppliedOffers(saleOrder, new ArrayList<>()));
    when(inventoryItemRepository.save(any())).thenAnswer(arg -> arg.getArgument(0));
    when(priceRepository.saveAll(any())).thenAnswer(arg -> arg.getArgument(0));
    doNothing().when(tranHeaderService).saveAndVerifyTranHeader(any(TranHeader.class));
    when(orderRepository.save(any())).thenAnswer(arg -> arg.getArgument(0));
    doReturn(new TranHeader()).when(orderServiceSpy).makeSale(any());

    // Act
    orderServiceSpy.processOrder(saleOrder);

    // Assert
    verify(orderRepository).save(argThat(order -> order.getOrderItems().get(0).getPrice() == 200.0)); // Should use
                                                                                                      // active price
  }

  @Test
  public void testProcessOrder_WhenDiscountAppliedIsMoreThanDiscountAllowed_ShouldThrowError() {
    // Arrange
    InventoryItem inventoryItem = getDummyInventoryItem();
    inventoryItem.setDiscountAllowed(5.0);
    inventoryItem.setId(1L);

    OrderItem orderItem = getDummyOrderItem();
    orderItem.setDiscount(10.0); // More than allowed
    orderItem.setInventoryItem(inventoryItem);

    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setOrderItems(List.of(orderItem));

    when(inventoryItemRepository.findById(anyLong())).thenReturn(Optional.of(inventoryItem));

    // Act & Assert
    assertThrows(NotAcceptableException.class, () -> orderService.processOrder(saleOrder));

    verify(inventoryItemRepository).findById(anyLong());
    verify(offerService, never()).getOffersToApply(any());
  }

  @Test
  public void testProcessOrder_WhenOffersArePresent_ShouldNotAdjustAmount() {
    // Arrange
    InventoryItem inventoryItem = getDummyInventoryItem();
    inventoryItem.setId(1L);

    OrderItem orderItem = getDummyOrderItem();
    orderItem.setDiscount(0.0);
    orderItem.setInventoryItem(inventoryItem);

    SaleOrder originalOrder = getDummySaleOrder();
    originalOrder.setOrderItems(List.of(orderItem));

    // Create modified order with offers applied (different total)
    SaleOrder modifiedOrder = getDummySaleOrder();
    modifiedOrder.setDiscount(20.0); // Offer discount
    modifiedOrder.setOrderItems(List.of(orderItem));

    when(inventoryItemRepository.findById(anyLong())).thenReturn(Optional.of(inventoryItem));
    when(offerService.getOffersToApply(any()))
        .thenReturn(new OrderWithDiscountsAndAppliedOffers(modifiedOrder, new ArrayList<>()));
    when(inventoryItemRepository.save(any())).thenAnswer(arg -> arg.getArgument(0));
    when(priceRepository.saveAll(any())).thenAnswer(arg -> arg.getArgument(0));
    doNothing().when(tranHeaderService).saveAndVerifyTranHeader(any(TranHeader.class));
    when(orderRepository.save(any())).thenAnswer(arg -> arg.getArgument(0));
    doReturn(new TranHeader()).when(orderServiceSpy).makeSale(any());

    // Act
    orderServiceSpy.processOrder(originalOrder);

    // Assert
    verify(offerService).getOffersToApply(any());
    verify(orderRepository).save(argThat(order -> order.getDiscount() == 20.0)); // Should preserve offer discount
  }

  @Test
  public void testFetchByDateRange_WithValidUser_ShouldReturnOrders() {
    // Arrange
    DateRange dateRange = getDummyDateRange();
    List<SaleOrder> expectedOrders = List.of(getDummySaleOrder());

    when(orderRepository.fetchAllByDateRangeAndShop(any(Date.class), any(Date.class), anyLong()))
        .thenReturn(expectedOrders);
    when(orderRepository.fetchAllSalesReturnedWithinRangeAndShop(any(Date.class), any(Date.class), anyLong()))
        .thenReturn(new ArrayList<>());

    // Act
    List<SaleOrder> result = orderService.fetchByDateRange(dateRange);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(orderRepository).fetchAllByDateRangeAndShop(
        dateRange.start(), dateRange.end(), getDummyUserShop().getShop().getId());
  }

  @Test
  public void testFetchByDateRange_WithNoCurrentUser_ShouldThrowException() {
    // Arrange
    DateRange dateRange = getDummyDateRange();
    when(auditorAware.getCurrentAuditor()).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NoSuchElementException.class, () -> orderService.fetchByDateRange(dateRange));
  }

  @Test
  public void testFetchByShopAndDateRange_ShouldReturnOrdersIncludingReturns() {
    // Arrange
    Shop shop = getDummyShop();
    DateRange dateRange = getDummyDateRange();

    List<SaleOrder> regularOrders = List.of(getDummySaleOrder());

    SaleOrder returnedOrder = getDummySaleOrder();
    OrderItem returnedItem = getDummyOrderItem();
    returnedItem.setReturnInward(getDummyReturnInward());
    returnedOrder.setOrderItems(List.of(returnedItem));
    List<SaleOrder> returnedOrders = List.of(returnedOrder);

    when(orderRepository.fetchAllByDateRangeAndShop(any(Date.class), any(Date.class), anyLong()))
        .thenReturn(regularOrders);
    when(orderRepository.fetchAllSalesReturnedWithinRangeAndShop(any(Date.class), any(Date.class), anyLong()))
        .thenReturn(returnedOrders);

    // Act
    List<SaleOrder> result = orderService.fetchByShopAndDateRange(shop, dateRange.start(), dateRange.end());

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size()); // Regular + returned orders
    verify(orderRepository).fetchAllByDateRangeAndShop(dateRange.start(), dateRange.end(), shop.getId());
    verify(orderRepository).fetchAllSalesReturnedWithinRangeAndShop(dateRange.start(), dateRange.end(), shop.getId());
  }

  @Test
  public void testProcessAmountDeduction_WithSufficientStock_ShouldDeductCorrectly() {
    // Arrange
    Price price1 = getDummyPrice();
    price1.setId(1);
    price1.setQuantityUnderThisPrice(5);

    Price price2 = getDummyPrice();
    price2.setId(2);
    price2.setQuantityUnderThisPrice(3);

    List<Price> prices = List.of(price1, price2);
    Integer requestedQuantity = 4;

    // Act
    var result = orderService.processAmountDeduction(requestedQuantity, prices);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(4, result.get(0).first.intValue());
    assertEquals(price1, result.get(0).second);
  }

  @Test
  public void testProcessAmountDeduction_WithInsufficientStock_ShouldThrowException() {
    // Arrange
    Price price1 = getDummyPrice();
    price1.setId(1);
    price1.setQuantityUnderThisPrice(2);

    List<Price> prices = List.of(price1);
    Integer requestedQuantity = 5; // More than available

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> orderService.processAmountDeduction(requestedQuantity, prices));

    assertEquals("Not enough stock", exception.getMessage());
  }

  @Test
  public void testProcessAmountDeduction_WithMultiplePrices_ShouldDeductFromMultiple() {
    // Arrange
    Price price1 = getDummyPrice();
    price1.setId(1);
    price1.setQuantityUnderThisPrice(2);

    Price price2 = getDummyPrice();
    price2.setId(2);
    price2.setQuantityUnderThisPrice(3);

    List<Price> prices = List.of(price1, price2);
    Integer requestedQuantity = 4;

    // Act
    var result = orderService.processAmountDeduction(requestedQuantity, prices);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(2, result.get(0).first.intValue()); // From first price
    assertEquals(2, result.get(1).first.intValue()); // From second price
  }

  @Test
  public void testReturnItem_WithValidRequest_ShouldProcessReturn() {
    // Arrange
    ReturnItemRequest request = getDummyReturnItemRequest();
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setId(request.orderItemId());
    orderItem.setQuantity(5); // More than return quantity
    orderItem.setReturnInward(null);

    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setDate(new Date()); // Recent sale
    saleOrder.setOrderItems(List.of(orderItem));

    when(orderItemRepository.findById(request.orderItemId()))
        .thenReturn(Optional.of(orderItem));
    when(orderRepository.findByOrderItems(orderItem))
        .thenReturn(Optional.of(saleOrder));
    when(returnInwardRepository.save(any(ReturnInward.class)))
        .thenAnswer(arg -> arg.getArgument(0));
    when(orderItemRepository.save(any(OrderItem.class)))
        .thenAnswer(arg -> arg.getArgument(0));
    doNothing().when(tranHeaderService).saveAndVerifyTranHeader(any(TranHeader.class));
    doReturn(new TranHeader()).when(orderServiceSpy).returnItemTransactions(any(), anyInt());

    // Act
    orderServiceSpy.returnItem(request);

    // Assert
    verify(orderItemRepository).findById(request.orderItemId());
    verify(orderRepository).findByOrderItems(orderItem);
    verify(returnInwardRepository).save(any(ReturnInward.class));
  }

  @Test
  public void testReturnItem_WithOldSale_ShouldThrowException() {
    // Arrange
    ReturnItemRequest request = getDummyReturnItemRequest();
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setId(request.orderItemId());

    SaleOrder saleOrder = getDummySaleOrder();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -31); // 31 days ago
    saleOrder.setDate(cal.getTime());

    when(orderItemRepository.findById(request.orderItemId()))
        .thenReturn(Optional.of(orderItem));
    when(orderRepository.findByOrderItems(orderItem))
        .thenReturn(Optional.of(saleOrder));

    // Act & Assert
    assertThrows(NotAcceptableException.class, () -> orderService.returnItem(request));
  }

  @Test
  public void testReturnItem_WithExcessiveQuantity_ShouldThrowException() {
    // Arrange
    ReturnItemRequest request = new ReturnItemRequest(1L, "Defective", 10, 0.0); // More than sold
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setId(request.orderItemId());
    orderItem.setQuantity(5); // Less than return quantity

    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setDate(new Date());

    when(orderItemRepository.findById(request.orderItemId()))
        .thenReturn(Optional.of(orderItem));
    when(orderRepository.findByOrderItems(orderItem))
        .thenReturn(Optional.of(saleOrder));

    // Act & Assert
    assertThrows(NotAcceptableException.class, () -> orderService.returnItem(request));
  }

  @Test
  public void testReturnItem_WithNonExistentOrderItem_ShouldThrowException() {
    // Arrange
    ReturnItemRequest request = getDummyReturnItemRequest();
    when(orderItemRepository.findById(request.orderItemId()))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NoSuchElementException.class, () -> orderService.returnItem(request));
  }

  @Test
  public void testReturnItem_WithAlreadyReturnedItem_ShouldUpdateExistingReturn() {
    // Arrange
    ReturnItemRequest request = getDummyReturnItemRequest();
    OrderItem orderItem = getDummyOrderItem();
    orderItem.setId(request.orderItemId());
    orderItem.setQuantity(5);

    ReturnInward existingReturn = getDummyReturnInward();
    existingReturn.setQuantityReturned(1);
    orderItem.setReturnInward(existingReturn);

    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setDate(new Date());

    when(orderItemRepository.findById(request.orderItemId()))
        .thenReturn(Optional.of(orderItem));
    when(orderRepository.findByOrderItems(orderItem))
        .thenReturn(Optional.of(saleOrder));
    when(returnInwardRepository.save(any(ReturnInward.class)))
        .thenAnswer(arg -> arg.getArgument(0));
    when(orderItemRepository.save(any(OrderItem.class)))
        .thenAnswer(arg -> arg.getArgument(0));
    doNothing().when(tranHeaderService).saveAndVerifyTranHeader(any(TranHeader.class));
    doReturn(new TranHeader()).when(orderServiceSpy).returnItemTransactions(any(), anyInt());

    // Act
    orderServiceSpy.returnItem(request);

    // Assert
    verify(returnInwardRepository).save(argThat(returnInward -> returnInward.getQuantityReturned() == 2)); // 1 existing
                                                                                                           // + 1 new
  }

  @Test
  public void testProcessOrder_WithNoCurrentUser_ShouldThrowException() {
    // Arrange
    SaleOrder saleOrder = getDummySaleOrder();
    when(auditorAware.getCurrentAuditor()).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NoSuchElementException.class, () -> orderService.processOrder(saleOrder));
  }

  @Test
  public void testProcessOrder_WithNonExistentInventoryItem_ShouldThrowException() {
    // Arrange
    OrderItem orderItem = getDummyOrderItem();
    orderItem.getInventoryItem().setId(999L);

    SaleOrder saleOrder = getDummySaleOrder();
    saleOrder.setOrderItems(List.of(orderItem));

    when(inventoryItemRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(RuntimeException.class, () -> orderService.processOrder(saleOrder));
  }
}
