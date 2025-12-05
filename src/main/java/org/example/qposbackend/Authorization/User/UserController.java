package org.example.qposbackend.Authorization.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.example.qposbackend.Authorization.Privileges.RequirePrivilege;
import org.example.qposbackend.Authorization.SystemUserDetails.UserDetailsServiceImpl;
import org.example.qposbackend.Authorization.User.dto.UserDto;
import org.example.qposbackend.DTOs.AuthRequest;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.DTOs.PasswordChange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

  @PostMapping("/create")
  public ResponseEntity<DataResponse> createUser(@Valid @RequestBody UserDto userDto) {
    try {
      User createdUser = userService.createUser(userDto);
      return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse(createdUser, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new DataResponse(null, e.getMessage()));
    }
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

  @GetMapping("/search")
  @RequirePrivilege(PrivilegesEnum.VIEW_USERS)
  public ResponseEntity<DataResponse> searchUser(@RequestParam String value) {
    try {
      Optional<User> user = userService.searchUser(value);
      return user.map(user1 -> ResponseEntity.ok(new DataResponse(user1, null)))
          .orElseGet(
              () ->
                  ResponseEntity.status(HttpStatus.NOT_FOUND)
                      .body(new DataResponse(null, "User not found")));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new DataResponse(null, e.getMessage()));
    }
  }
}
