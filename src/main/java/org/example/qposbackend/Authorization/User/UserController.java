package org.example.qposbackend.Authorization.User;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.example.qposbackend.Authorization.SystemUserDetails.UserDetailsServiceImpl;
import org.example.qposbackend.DTOs.AuthRequest;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.DTOs.PasswordChange;
import org.example.qposbackend.Security.Jwt.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {
    private final UserDetailsServiceImpl service;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;
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
    public ResponseEntity<DataResponse> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.email().trim(), authRequest.password().trim()));
            User user = userRepository.findUserByEmail(authRequest.email()).get();
            System.out.println("User is " + user);
            String token = jwtUtil.generateToken((SystemUserDetails) authentication.getPrincipal());
            LoginResponse response = new LoginResponse(token, user);
            return ResponseEntity.ok(new DataResponse(response, null));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DataResponse(null, "Invalid!"));
        }
    }

    @PostMapping("change-password")
    public ResponseEntity<MessageResponse> changePassword(@RequestBody PasswordChange passwordChange) {
        try {
            userService.updatePassword(passwordChange);
            return ResponseEntity.ok(new MessageResponse("Password changed!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage()));
        }
    }



}

record LoginResponse(String token, User user) {
}