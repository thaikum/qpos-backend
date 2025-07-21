package org.example.qposbackend.Accounting.Accounts;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("accounts")
public class AccountsController {
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<MessageResponse> createAccount(@RequestBody Account account) {
        try{
            accountService.createAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Account created successfully"));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<DataResponse> getAccounts() {
        List<Account> accounts = accountService.findAllAccountsByShop();
        return ResponseEntity.ok(new DataResponse(accounts, null));
    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateAccount(@RequestBody Account account) {
        try{
            accountService.updateAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Account updated successfully"));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage()));
        }
    }
}
