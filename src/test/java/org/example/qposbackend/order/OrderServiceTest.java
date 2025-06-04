package org.example.qposbackend.order;

import org.assertj.core.util.Streams;
import org.example.qposbackend.Accounting.Accounts.AccountRepository;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeader;
import org.example.qposbackend.Accounting.Transactions.TranHeader.TranHeaderService;
import org.example.qposbackend.EOD.EODRepository;
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
import org.example.qposbackend.order.orderItem.ReturnInward.ReturnInwardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
  @Mock private OrderRepository orderRepository;
  @Mock private InventoryItemRepository inventoryItemRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private TranHeaderService tranHeaderService;
  @Mock private SpringSecurityAuditorAware auditorAware;
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private ReturnInwardRepository returnInwardRepository;
  @Mock private EODRepository eodRepository;
  @Mock private OfferService offerService;
  @Mock private PriceRepository priceRepository;
  @InjectMocks private OrderService orderService;
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
                  Optional<Price> priceOptional =
                      Streams.stream(args).filter(p -> p.getId() == 3).findAny();
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
                  Optional<Price> priceOptional =
                      Streams.stream(args).filter(p -> p.getId() == 1).findAny();
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

  public void testProcessOrder_WhenActivePriceIsNotTheLast_SellingPriceShouldOfTheActivePrice() {}

  public void testProcessOrder_WhenDiscoutAppliedIsMoreThanDiscountAllowed_ShouldThrowError() {}

  public void testProcessOrder_WhenOffersArePresent_ShouldNotAdjustAmount() {}
}
