package org.example.qposbackend.Authorization.SystemUserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class SystemUserDetails implements UserDetails {
  private final UserShop userShop;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return userShop.getRole().getPrivileges().stream()
        .map(privilege -> new SimpleGrantedAuthority(privilege.getPrivilege()))
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return userShop.getUser().getActivePassword();
  }

  @Override
  public String getUsername() {
    return userShop.getUser().getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return userShop.getUser().getEnabled();
  }

  public String getShopCode() {
    return userShop.getShop().getCode();
  }
}
