package org.example.qposbackend.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.example.qposbackend.Authorization.Privileges.RequirePrivilege;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("customers")
public class CustomerController {
  private final CustomerService customerService;

  @PostMapping
  @RequirePrivilege(value = {PrivilegesEnum.ADD_CUSTOMERS})
  public ResponseEntity<DataResponse> createCustomer(@RequestBody @Valid Customer customer) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new DataResponse(
                customerService.createCustomer(customer), "User created successfully!"));
  }

  @GetMapping
  @RequirePrivilege(value = {PrivilegesEnum.VIEW_CUSTOMERS})
  public ResponseEntity<?> fetchCustomers() {
    return ResponseEntity.ok(new DataResponse(customerService.getCustomers(), null));
  }
}
