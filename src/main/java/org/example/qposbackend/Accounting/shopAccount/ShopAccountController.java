package org.example.qposbackend.Accounting.shopAccount;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shop-accounts")
@RequiredArgsConstructor
public class ShopAccountController {
    private final ShopAccountService shopAccountService;

    @PostMapping("/{accountId}")
    public ResponseEntity<?> createShopAccount(@PathVariable Long accountId) {
        try {
            ShopAccount shopAccount = shopAccountService.createShopAccount(accountId);
            return ResponseEntity.ok(shopAccount);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getShopAccounts() {
        try {
            return ResponseEntity.ok(shopAccountService.getShopAccounts());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShopAccount(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(shopAccountService.getShopAccount(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleAccountStatus(@PathVariable Long id) {
        try {
            shopAccountService.toggleAccountStatus(id);
            return ResponseEntity.ok(new MessageResponse("Account status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}