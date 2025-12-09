package org.example.qposbackend.order;

import org.example.qposbackend.order.orderItem.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<SaleOrder, Long> {
  @Query(
      value =
          "SELECT so.* FROM sale_order so WHERE so.shop_id = :shopId AND DATE(so.date) BETWEEN DATE(:start) AND DATE(:end)",
      nativeQuery = true)
  List<SaleOrder> fetchAllByDateRangeAndShop(Date start, Date end, Long shopId);

  @Query(
      value =
          "SELECT so.* "
              + "FROM sale_order so "
              + "JOIN order_item oi ON so.id = oi.order_items_id "
              + "JOIN return_inward ri ON oi.return_inward_id = ri.id "
              + "WHERE so.shop_id = :shopId AND DATE(ri.date_returned) BETWEEN DATE(:start) AND DATE(:end) "
              + "AND DATE(ri.date_sold) NOT BETWEEN DATE(:start) AND DATE(:end)",
      nativeQuery = true)
  List<SaleOrder> fetchAllSalesReturnedWithinRangeAndShop(Date start, Date end, Long shopId);

  Optional<SaleOrder> findByOrderItems(OrderItem orderId);
}
