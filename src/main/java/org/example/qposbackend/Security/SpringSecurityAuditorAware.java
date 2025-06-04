package org.example.qposbackend.Security;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.core.ApplicationContext;
import org.example.qposbackend.Authorization.SystemUserDetails.SystemUserDetails;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.UserRepository;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Authorization.User.userShop.UserShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNullApi;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<UserShop> {
  @Autowired private UserShopRepository userShopRepository;

  @Override
  public Optional<UserShop> getCurrentAuditor() {
    try {
      Optional<SystemUserDetails> userDetailsOptional =
          Optional.ofNullable(SecurityContextHolder.getContext())
              .map(SecurityContext::getAuthentication)
              .filter(Authentication::isAuthenticated)
              .map(Authentication::getPrincipal)
              .filter(SystemUserDetails.class::isInstance)
              .map(SystemUserDetails.class::cast);

      return userDetailsOptional.map(SystemUserDetails::getUserShop);
    } catch (Exception ex) {
      return Optional.empty();
    }
  }
}
