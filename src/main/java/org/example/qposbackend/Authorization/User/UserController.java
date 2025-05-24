package org.example.qposbackend.Authorization.User;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.SystemUserDetails.UserDetailsServiceImpl;
import org.example.qposbackend.DTOs.AuthRequest;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.DTOs.PasswordChange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {
  private final UserDetailsServiceImpl service;

  private final UserRepository userRepository;
  private final UserService userService;

  @GetMapping
  public ResponseEntity<DataResponse> getUsers() {
    return ResponseEntity.ok(new DataResponse(userRepository.findAll(), null));
  }

  @PostMapping
  public String addNewUser(@RequestBody User user) {
    return service.addUser(user);
  }

  @PostMapping("/login")
  public ResponseEntity<DataResponse> authenticateAndGetToken(
      @RequestBody AuthRequest authRequest) {

    try {
      return ResponseEntity.ok(new DataResponse(userService.authenticateUser(authRequest), null));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new DataResponse(null, ex.getMessage()));
    }
  }

  @PostMapping("change-password")
  public ResponseEntity<MessageResponse> changePassword(
      @RequestBody PasswordChange passwordChange) {
    try {
      userService.updatePassword(passwordChange);
      return ResponseEntity.ok(new MessageResponse("Password changed!"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage()));
    }
  }
}
