package org.example.qposbackend.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.hirePurchase.HirePurchaseService;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
  private final CustomerRepository customerRepository;
  private final AuthUserShopProvider authProvider;
  private final HirePurchaseService hirePurchaseService;

  public Customer createCustomer(Customer customer) {
    customer.setShop(authProvider.getCurrentShop());
    return save(customer);
  }

  public List<Customer> getCustomers() {
    Shop shop = authProvider.getCurrentShop();
    return customerRepository.findAllByShop(shop).stream()
        .peek(
            customer -> {
              Double totalDebt = hirePurchaseService.getTotalDebtPerCustomer(customer);
              customer.setReceivables(totalDebt);
            })
        .toList();
  }

  private Customer save(@Valid Customer customer) {
    return customerRepository.save(customer);
  }
}
