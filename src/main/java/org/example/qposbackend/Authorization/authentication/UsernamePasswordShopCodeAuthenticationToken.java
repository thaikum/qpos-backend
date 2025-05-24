package org.example.qposbackend.Authorization.authentication;

import java.io.Serial;
import java.util.Collection;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;

@Getter
public class UsernamePasswordShopCodeAuthenticationToken extends AbstractAuthenticationToken {
  @Serial private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  private final Object principal;

  private Object credentials;
  private final String shopCode;

  public UsernamePasswordShopCodeAuthenticationToken(
      Object principal, Object credentials, String shopCode) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    this.shopCode = shopCode;
    setAuthenticated(false);
  }

  public UsernamePasswordShopCodeAuthenticationToken(
      Object principal,
      Object credentials,
      String shop,
      Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    this.shopCode = shop;
    super.setAuthenticated(true); // must use super, as we override
  }

  public static UsernamePasswordShopCodeAuthenticationToken unauthenticated(
      Object principal, Object credentials, String shopCode) {
    return new UsernamePasswordShopCodeAuthenticationToken(principal, credentials, shopCode);
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    Assert.isTrue(
        !isAuthenticated,
        "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
    super.setAuthenticated(false);
  }

  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
    this.credentials = null;
  }
}
