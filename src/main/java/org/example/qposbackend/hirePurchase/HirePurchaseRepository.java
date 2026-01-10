package org.example.qposbackend.hirePurchase;

import org.example.qposbackend.customer.Customer;
import org.example.qposbackend.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HirePurchaseRepository extends JpaRepository<HirePurchase, Long> {
    List<HirePurchase> findAllByStartDateBetween(LocalDate from, LocalDate to);
    List<HirePurchase> findAllByStatusAndShopAndCustomer(HirePurchaseStatus status, Shop shop, Customer customer);

    @Modifying()
    @Query(value = "UPDATE HirePurchase set status = :status, totalPaidAmount = :totalPaidAmount where id = :id")
    void updateStatusAndTotalPaidAmountById(HirePurchaseStatus status, Double totalPaidAmount, Long id);
}
