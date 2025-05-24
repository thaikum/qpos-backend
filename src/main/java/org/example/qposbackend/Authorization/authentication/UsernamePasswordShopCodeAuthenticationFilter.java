package org.example.qposbackend.Authorization.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

public class UsernamePasswordShopCodeAuthenticationFilter
    extends AbstractAuthenticationProcessingFilter implements Ordered {
  public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";

  public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

  public static final String SPRING_SECURITY_FORM_SHOP_CODE_KEY = "shopCode";

  private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
      new AntPathRequestMatcher("/login", "POST");

  private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;

  private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;

  private String shopCodeParameter = SPRING_SECURITY_FORM_SHOP_CODE_KEY;

  private boolean postOnly = true;

  public UsernamePasswordShopCodeAuthenticationFilter() {
    super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
  }

  public UsernamePasswordShopCodeAuthenticationFilter(AuthenticationManager authenticationManager) {
    super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    if (this.postOnly && !request.getMethod().equals("POST")) {
      throw new AuthenticationServiceException(
          "Authentication method not supported: " + request.getMethod());
    }
    String username = obtainUsername(request);
    username = (username != null) ? username.trim() : "";
    String password = obtainPassword(request);
    password = (password != null) ? password : "";
    String shopCode = obtainShopCode(request);
    shopCode = (shopCode != null) ? shopCode.trim() : "";

    UsernamePasswordShopCodeAuthenticationToken authRequest =
        UsernamePasswordShopCodeAuthenticationToken.unauthenticated(username, password, shopCode);
    setDetails(request, authRequest);
    return this.getAuthenticationManager().authenticate(authRequest);
  }

  @Nullable
  protected String obtainPassword(HttpServletRequest request) {
    return request.getParameter(this.passwordParameter);
  }

  @Nullable
  protected String obtainUsername(HttpServletRequest request) {
    return request.getParameter(this.usernameParameter);
  }

  @Nullable
  protected String obtainShopCode(HttpServletRequest request) {
    return request.getParameter(this.shopCodeParameter);
  }

  protected void setDetails(
      HttpServletRequest request, UsernamePasswordShopCodeAuthenticationToken authRequest) {
    authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
  }

  public void setUsernameParameter(String usernameParameter) {
    Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
    this.usernameParameter = usernameParameter;
  }

  public void setPasswordParameter(String passwordParameter) {
    Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
    this.passwordParameter = passwordParameter;
  }

  public void setPostOnly(boolean postOnly) {
    this.postOnly = postOnly;
  }

  public final String getUsernameParameter() {
    return this.usernameParameter;
  }

  public final String getPasswordParameter() {
    return this.passwordParameter;
  }

  @Override
  public int getOrder() {
    return SecurityProperties.DEFAULT_FILTER_ORDER - 2; // Or any appropriate order
  }
}
