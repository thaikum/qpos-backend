package org.example.qposbackend.customer;

import org.example.qposbackend.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findCustomerByIdAndShop_Id(Long customerId, Long shopId);

    List<Customer> findAllByShop(Shop shop);
}
