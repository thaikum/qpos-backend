package org.example.qposbackend.Authorization.User;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.Roles.SystemRoleRepository;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.example.qposbackend.Authorization.User.Password.Password;
import org.example.qposbackend.Authorization.User.dto.LoginResponse;
import org.example.qposbackend.Authorization.User.dto.UserCredentials;
import org.example.qposbackend.DTOs.AuthRequest;
import org.example.qposbackend.DTOs.PasswordChange;
import org.example.qposbackend.Security.Jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final SystemRoleRepository systemRoleRepository;
  private final AuthenticationManager authenticationManager;

  public LoginResponse authenticateUser(AuthRequest authRequest) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  new UserCredentials(authRequest.email().trim(), authRequest.shopCode()),
                  authRequest.password().trim()));

      log.info("Authentication is: {}", authentication);

      User user = userRepository.findUserByEmail(authRequest.email()).orElseThrow();
      String token = jwtUtil.generateToken((SystemUserDetails) authentication.getPrincipal());
      return new LoginResponse(token, user);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new RuntimeException(ex.getMessage());
    }
  }

  public void updatePassword(PasswordChange passwordChange) {
    Optional<UserDetails> userDetailsOptional =
        Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .map(UserDetails.class::cast);

    if (userDetailsOptional.isPresent()) {
      Optional<User> user = userRepository.findUserByEmail(userDetailsOptional.get().getUsername());

      if (user.isPresent()) {
        List<Password> passwords = user.get().getPasswords();
        if (passwords.size() == 12) {
          passwords.remove(0);
        }
        passwords.add(
            Password.builder().password(passwordEncoder.encode(passwordChange.password())).build());
        User newUser = user.get();
        newUser.setPasswords(passwords);
        userRepository.save(newUser);
      } else {
        throw new RuntimeException("User not found");
      }
    } else {
      throw new RuntimeException("User not found");
    }
  }

  @Bean
  public void createInitialUser() {
    Optional<SystemRole> optionalSystemRole = systemRoleRepository.findById("ADMIN");

    if (optionalSystemRole.isPresent() && userRepository.findAll().isEmpty()) {
      SystemRole systemRole = optionalSystemRole.get();

      User user =
          User.builder()
              .email("fredthaiku@gmail.com")
              .enabled(true)
              .firstName("Fredrick")
              .lastName("Thaiku")
              .idType(IdType.NATIONAL_ID)
              .passwords(
                  List.of(
                      Password.builder()
                          .password(passwordEncoder.encode("@Fredsystem5647"))
                          .build()))
              .role(systemRole)
              .build();

      userRepository.save(user);
      log.info("Initial user created");
    }
  }
}
