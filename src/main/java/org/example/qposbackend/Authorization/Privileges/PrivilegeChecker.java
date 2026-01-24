package org.example.qposbackend.Authorization.Privileges;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service bean that performs privilege checking for the @RequirePrivilege annotation. This class is
 * used by the PrivilegeAspect to check user privileges.
 */
@Component("privilegeChecker")
public class PrivilegeChecker {

  /**
   * Checks if user has any of the specified privileges.
   *
   * @param authentication The authentication object
   * @param requiredPrivileges Array of required privileges
   * @return true if user has at least one of the privileges
   */
  public boolean hasAnyPrivilege(
      Authentication authentication, PrivilegesEnum[] requiredPrivileges) {
    Set<String> userPrivileges = getPrivileges(authentication);
    if (userPrivileges == null) return false;

    return Arrays.stream(requiredPrivileges)
        .anyMatch(privilege -> userPrivileges.contains(privilege.name()));
  }

  /**
   * Checks if user has all of the specified privileges.
   *
   * @param authentication The authentication object
   * @param requiredPrivileges Array of required privileges
   * @return true if user has all of the privileges
   */
  public boolean hasAllPrivileges(
      Authentication authentication, PrivilegesEnum[] requiredPrivileges) {
    Set<String> userPrivileges = getPrivileges(authentication);
    if (userPrivileges == null) return false;

    return Arrays.stream(requiredPrivileges)
        .allMatch(privilege -> userPrivileges.contains(privilege.name()));
  }

  /**
   * Checks if user has a single privilege.
   *
   * @param authentication The authentication object
   * @param requiredPrivilege The required privilege
   * @return true if user has the privilege
   */
  public boolean hasPrivilege(Authentication authentication, PrivilegesEnum requiredPrivilege) {
    return hasAnyPrivilege(authentication, new PrivilegesEnum[] {requiredPrivilege});
  }

  /**
   * Helper method to extract user privileges from authentication object.
   *
   * @param authentication The authentication object
   * @return Set of privilege names, or null if authentication is invalid
   */
  private static Set<String> getPrivileges(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
  }
}
