package org.example.qposbackend.Accounting.shopAccount;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Accounting.shopAccount.dto.ShopAccountDto;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.example.qposbackend.Authorization.Privileges.RequirePrivilege;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("shop-accounts")
@RequiredArgsConstructor
public class ShopAccountController {
  private final ShopAccountService shopAccountService;

  @PostMapping
  public ResponseEntity<MessageResponse> createShopAccount(
      @RequestBody ShopAccount shopAccount) {
    try {
      shopAccountService.createShopAccount(shopAccount);
      return ResponseEntity.ok(new MessageResponse("Account created successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    }
  }

  @GetMapping
  @RequirePrivilege(PrivilegesEnum.VIEW_ACCOUNTS)
  public ResponseEntity<?> getShopAccounts() {
    try {
      return ResponseEntity.ok(new DataResponse(shopAccountService.getShopAccounts(), null));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    }
  }

  @RequirePrivilege(PrivilegesEnum.VIEW_ACCOUNTS)
  @GetMapping("/{id}")
  public ResponseEntity<?> getShopAccount(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(new DataResponse(shopAccountService.getShopAccount(id), null));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    }
  }

  @PutMapping("update/{id}")
  @RequirePrivilege(PrivilegesEnum.UPDATE_ACCOUNT)
  public ResponseEntity<?> updateShopAccount(@PathVariable("id") long id, @RequestBody ShopAccountDto shopAccount) {
    try{
      return ResponseEntity.ok(new DataResponse(shopAccountService.updateAccount(id, shopAccount), null));
    }catch (Exception e){
      return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    }
  }

  @PutMapping("/{id}/toggle-status")
  public ResponseEntity<?> toggleAccountStatus(@PathVariable Long id) {
    try {
      shopAccountService.toggleAccountStatus(id);
      return ResponseEntity.ok(new MessageResponse("Account status updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    }
  }
}
