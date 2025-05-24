package org.example.qposbackend.Authorization.authentication;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Authorization.User.userShop.UserShopRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("StoqItAuthenticationProvider")
@RequiredArgsConstructor
public class StoqItAuthenticationProvider implements AuthenticationProvider {
  private final UserShopRepository userShopRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (!(authentication instanceof UsernamePasswordShopCodeAuthenticationToken customToken)) {
      return null;
    }

    String email = customToken.getName();
    String password = customToken.getCredentials().toString();
    String shopCode = customToken.getShopCode();

    Optional<UserShop> userShopOptional =
        userShopRepository.findUserShopByShop_CodeAndUser_email(shopCode, email);

    if (userShopOptional.isEmpty()) {
      throw new UsernameNotFoundException("User not found with email and shop code");
    }

    UserShop userShop = userShopOptional.get();
    User user = userShop.getUser();

    if (!passwordEncoder.matches(password, user.getActivePassword())) {
      throw new BadCredentialsException("Invalid password for user: " + email);
    }

    if (userShop.getShop() == null || !userShop.getShop().getCode().equals(shopCode)) {
      throw new BadCredentialsException("User does not belong to shop: " + shopCode);
    }

    SystemUserDetails userDetails = new SystemUserDetails(userShop);

    return new UsernamePasswordShopCodeAuthenticationToken(
        userDetails, password, shopCode, userDetails.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordShopCodeAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
