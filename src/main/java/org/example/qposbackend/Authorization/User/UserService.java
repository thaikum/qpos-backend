package org.example.qposbackend.Authorization.User;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.Roles.SystemRoleRepository;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.example.qposbackend.Authorization.User.Password.Password;
import org.example.qposbackend.Authorization.User.dto.LoginResponse;
import org.example.qposbackend.Authorization.User.dto.UserCredentials;
import org.example.qposbackend.Authorization.User.dto.UserDto;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Authorization.User.userShop.UserShopRepository;
import org.example.qposbackend.DTOs.AuthRequest;
import org.example.qposbackend.DTOs.PasswordChange;
import org.example.qposbackend.Security.Jwt.JwtUtil;
import org.example.qposbackend.shop.Shop;
import org.example.qposbackend.shop.ShopRepository;
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
public class UserService {
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final SystemRoleRepository systemRoleRepository;
  private final AuthenticationManager authenticationManager;
  private final UserShopRepository userShopRepository;
  private final ShopRepository shopRepository;

  public LoginResponse authenticateUser(AuthRequest authRequest) {
    try {
      Shop shop;
      Optional<Shop> optionalShop = shopRepository.findShopByCode(authRequest.shopCode());
      UserShop userShop = null;

      if (optionalShop.isEmpty()) {
        userShop =
            userShopRepository
                .findDefaultUserShopByUser_email(authRequest.email())
                .orElseThrow(() -> new RuntimeException("User not found with email and shop code"));
        shop = userShop.getShop();
      } else {
        shop = optionalShop.get();
      }
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  new UserCredentials(authRequest.email().trim(), authRequest.shopCode()),
                  authRequest.password().trim()));

      User user = userRepository.findUserByEmail(authRequest.email()).orElseThrow();
      String token = jwtUtil.generateToken((SystemUserDetails) authentication.getPrincipal());

      if( userShop == null ){
        userShop = userShopRepository.getByUserAndShop_Code(user, authRequest.shopCode());
      }

      return new LoginResponse(token, userShop);
    } catch (Exception ex) {
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
          passwords.removeFirst();
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

  public User createUser(UserDto userDto) {
    if (userRepository.findUserByEmail(userDto.getEmail()).isPresent()) {
      throw new RuntimeException("User with email " + userDto.getEmail() + " already exists");
    }

    String defaultPassword = userDto.getPassword() != null ? userDto.getPassword() : "12345678";
    Password password =
        Password.builder().password(passwordEncoder.encode(defaultPassword)).build();

    User user =
        User.builder()
            .email(userDto.getEmail().trim())
            .firstName(userDto.getFirstName())
            .lastName(userDto.getLastName())
            .phoneNumber(userDto.getPhoneNumber())
            .idType(userDto.getIdType())
            .idNumber(userDto.getIdNumber())
            .passwords(List.of(password))
            .enabled(true)
            .isLoggedIn(false)
            .build();

      return userRepository.save(user);
  }

  public Optional<User> searchUser(String value) {
    return userRepository.findUserByIdNumberOrEmail(value.trim());
  }

  @Bean
  public void createInitialUser() {
    Optional<SystemRole> optionalSystemRole = systemRoleRepository.findById("ADMIN");

    if (optionalSystemRole.isPresent() && userRepository.findAll().isEmpty()) {

      User user =
          User.builder()
              .email("fredthaiku@gmail.com")
              .enabled(true)
              .firstName("Fredrick")
              .lastName("Thaiku")
              .phoneNumber("+254700000000")
              .idType(IdType.NATIONAL_ID)
              .idNumber("12345678")
              .passwords(
                  List.of(
                      Password.builder()
                          .password(passwordEncoder.encode("@Fredsystem5647"))
                          .build()))
              .build();

      userRepository.save(user);
    }
  }
}
