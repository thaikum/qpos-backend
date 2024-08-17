package org.example.qposbackend.Order;

import org.example.qposbackend.Order.OrderItem.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<SaleOrder, Long> {
    @Query(nativeQuery = true, value = "select * from sale_order where DATE(date) between DATE(:start) and DATE(:end)")
    List<SaleOrder> fetchAllByDateRange(Date start, Date end);

    @Query(nativeQuery = true, value = "select so.* " +
            "            from sale_order so " +
            "                     join order_item oi on so.id = oi.order_items_id " +
            "                     join return_inward ri on oi.return_inward_id = ri.id " +
            "            where DATE(ri.date_returned) between DATE(:start) and DATE(:end) " +
            "              and DATE(date_sold) not between DATE(:start) and DATE(:end)")
    List<SaleOrder> fetchAllSalesReturnedWithinRange(Date start, Date end);

    Optional<SaleOrder> findByOrderItems(OrderItem orderId);
}
