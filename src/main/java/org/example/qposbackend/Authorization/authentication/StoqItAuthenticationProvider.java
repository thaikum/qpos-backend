package org.example.qposbackend.Authorization.authentication;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.dto.UserCredentials;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Authorization.User.userShop.UserShopRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    if (!(authentication instanceof UsernamePasswordAuthenticationToken customToken)) {
      return null;
    }

    UserCredentials credentials = (UserCredentials) customToken.getPrincipal();
    String password = customToken.getCredentials().toString();
    String shopCode = credentials.shopCode();
    String email = credentials.email();

    Optional<UserShop> userShopOptional =
        userShopRepository.findUserShopByShop_CodeAndUser_email(shopCode, email);

    if (userShopOptional.isEmpty()) {
      userShopOptional = userShopRepository.findDefaultUserShopByUser_email(email);
    }

    if (userShopOptional.isEmpty()) {
      throw new UsernameNotFoundException("User not found with email and shop code");
    }

    UserShop userShop = userShopOptional.get();
    User user = userShop.getUser();

    if (!passwordEncoder.matches(password, user.getActivePassword())) {
      throw new BadCredentialsException("Invalid password for user: " + email);
    }

    SystemUserDetails userDetails = new SystemUserDetails(userShop);

    return new UsernamePasswordAuthenticationToken(
        userDetails, password, userDetails.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
