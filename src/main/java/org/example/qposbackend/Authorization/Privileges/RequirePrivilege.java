package org.example.qposbackend.Authorization.Privileges;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation that provides a convenient way to use @PreAuthorize with PrivilegesEnum. This
 * annotation automatically converts PrivilegesEnum values to the appropriate @PreAuthorize
 * expression.
 *
 * <p>Usage examples: 
 * @RequirePrivilege(PrivilegesEnum.VIEW_USERS) 
 * @RequirePrivilege({PrivilegesEnum.VIEW_USERS, PrivilegesEnum.ADD_USER}) // requires ANY of these privileges 
 * @RequirePrivilege(value = {PrivilegesEnum.VIEW_USERS, PrivilegesEnum.ADD_USER}, requireAll = true) // requires ALL privileges
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePrivilege {

  /**
   * The privilege(s) required to access the annotated method or class. If multiple privileges are
   * specified, by default ANY of them will grant access. Use requireAll = true to require ALL
   * privileges.
   */
  PrivilegesEnum[] value();

  /**
   * When multiple privileges are specified: - false (default): User needs ANY of the specified
   * privileges - true: User needs ALL of the specified privileges
   */
  boolean requireAll() default false;
}
