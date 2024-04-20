package org.example.qposbackend.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<SaleOrder, Long> {
    @Query(nativeQuery = true, value = "select * from sale_order where DATE(date) between DATE(:start) and DATE(:end)")
    List<SaleOrder> fetchAllByDateRange(Date start, Date end);
}
