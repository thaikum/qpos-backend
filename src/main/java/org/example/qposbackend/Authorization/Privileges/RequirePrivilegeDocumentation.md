# @RequirePrivilege Annotation Documentation

## Overview

The `@RequirePrivilege` annotation is a custom security annotation that provides a convenient way to handle privilege checking using `PrivilegesEnum` values. This annotation works alongside Spring Security's HTTP-level security to provide method-level privilege enforcement.

## Features

- **Type-safe privilege checking** using `PrivilegesEnum`
- **Flexible privilege requirements** (ANY or ALL of specified privileges)
- **Method and class-level annotation support**
- **Integration with Spring Security**
- **Automatic privilege validation with comprehensive logging**
- **Works with HTTP-level authentication** - requests must pass HTTP security first

## Architecture

The system uses a hybrid approach:

1. **HTTP Security Layer**: Spring Security's `HttpSecurity` configuration handles authentication and basic authorization
2. **Method Security Layer**: `@RequirePrivilege` annotation provides fine-grained privilege checking via AOP

### Components

1. **@RequirePrivilege Annotation**: The main annotation for protecting methods and classes
2. **PrivilegeAspect**: AOP aspect that intercepts annotated methods
3. **PrivilegeChecker**: Service that performs the actual privilege validation
4. **AspectConfig**: Configuration that enables AspectJ auto-proxy
5. **HttpConfigurer**: Updated to work with the annotation system

## HTTP Security Configuration

The HTTP security is configured to:
- Allow specific endpoints with specific privileges (existing behavior)
- Allow `user-shops/**` endpoints with basic authentication
- Let `@RequirePrivilege` handle the detailed privilege checking for these endpoints
- Require authentication for all other requests (changed from `permitAll()`)

```java
// In HttpConfigurer.java
.requestMatchers("user-shops/**")
.authenticated() // Let @RequirePrivilege handle the specific privilege checks

.anyRequest()
.authenticated() // Changed from permitAll() to authenticated()
```

## Usage Examples

### Basic Usage - Single Privilege
```java
@GetMapping("/users")
@RequirePrivilege(PrivilegesEnum.VIEW_USERS)
public ResponseEntity<DataResponse> getUsers() {
    // Only users with VIEW_USERS privilege can access this endpoint
    return ResponseEntity.ok(new DataResponse(userService.getAllUsers(), null));
}
```

### Multiple Privileges - ANY (Default behavior)
```java
@PutMapping("/user-shops")
@RequirePrivilege({PrivilegesEnum.ADD_USER, PrivilegesEnum.VIEW_USERS})
public ResponseEntity<DataResponse> updateUserShop(@RequestBody UpdateUserShopRequest request) {
    // User needs either ADD_USER OR VIEW_USERS privilege
    return ResponseEntity.ok(new DataResponse(userShopService.updateUserShop(request), null));
}
```

### Multiple Privileges - ALL Required
```java
@DeleteMapping("/user-shops/{id}")
@RequirePrivilege(value = {PrivilegesEnum.ADD_USER, PrivilegesEnum.VIEW_USERS}, requireAll = true)
public ResponseEntity<MessageResponse> deleteUserShop(@PathVariable Long id) {
    // User needs both ADD_USER AND VIEW_USERS privileges
    userShopService.deleteUserShop(id);
    return ResponseEntity.ok(new MessageResponse("UserShop deleted successfully"));
}
```

### Class-Level Annotation
```java
@RestController
@RequestMapping("/admin")
@RequirePrivilege(PrivilegesEnum.VIEW_ADMIN_PARAMETERS)
public class AdminController {
    
    // All methods in this controller require VIEW_ADMIN_PARAMETERS privilege
    
    @GetMapping("/settings")
    public ResponseEntity<DataResponse> getSettings() {
        // Inherits privilege requirement from class-level annotation
        return ResponseEntity.ok(new DataResponse(adminService.getSettings(), null));
    }
    
    @PostMapping("/settings")
    @RequirePrivilege(PrivilegesEnum.UPDATE_ADMIN_PARAMETERS) // Method-level overrides class-level
    public ResponseEntity<DataResponse> updateSettings(@RequestBody AdminSettings settings) {
        // This method requires UPDATE_ADMIN_PARAMETERS instead of VIEW_ADMIN_PARAMETERS
        return ResponseEntity.ok(new DataResponse(adminService.updateSettings(settings), null));
    }
}
```

## How It Works

### Request Flow

1. **HTTP Security Check**: Spring Security first validates the request against HTTP security rules
2. **Authentication**: User must be authenticated (JWT token validation)
3. **Method Invocation**: If HTTP security passes, the method is invoked
4. **AOP Interception**: `PrivilegeAspect` intercepts the method call
5. **Privilege Check**: `PrivilegeChecker` validates user privileges against annotation requirements
6. **Access Decision**: Method executes if privileges are sufficient, otherwise `AccessDeniedException` is thrown

### Example: UserShop Controller

```java
@RestController
@RequestMapping("user-shops")
public class UserShopController {
    
    @GetMapping
    @RequirePrivilege(PrivilegesEnum.VIEW_USERS)
    public ResponseEntity<DataResponse> getAllUserShops() {
        // 1. HTTP Security: Checks if user is authenticated for "user-shops/**"
        // 2. AOP Aspect: Checks if user has VIEW_USERS privilege
        // 3. Method executes if both checks pass
    }
}
```

## Configuration Requirements

### 1. Enable Method Security
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Required for method-level security
public class HttpConfigurer {
    // ...
}
```

### 2. Enable AspectJ Auto-Proxy
```java
@Configuration
@EnableAspectJAutoProxy // Required for @RequirePrivilege to work
public class AspectConfig {
}
```

### 3. HTTP Security Configuration
```java
.requestMatchers("user-shops/**")
.authenticated() // Basic authentication check

.anyRequest()
.authenticated() // Require authentication for all requests
```

## Error Handling

### Access Denied Response
When a user lacks required privileges:

```json
{
  "status": 403,
  "error": "Forbidden", 
  "message": "Insufficient privileges to access this resource"
}
```

### Authentication Required Response
When a user is not authenticated:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

## Logging

The system provides detailed logging:

```
WARN  - Access denied: User 'john@example.com' lacks required privileges for method: UserShopController.deleteUserShop(..). Required: [ADD_USER, VIEW_USERS], RequireAll: true
DEBUG - Access granted: User 'admin@example.com' has required privileges for method: UserShopController.getAllUsers(..)
```

## Benefits Over Pure @PreAuthorize

1. **Type Safety**: Compile-time checking prevents privilege name typos
2. **Readability**: More readable than SpEL expressions
3. **Maintainability**: Easy to refactor privilege names
4. **IDE Support**: Auto-completion and navigation
5. **Consistency**: Standardized privilege handling
6. **Flexibility**: Support for both ANY and ALL logic

## Migration Guide

### From permitAll() to authenticated()

**Before:**
```java
.anyRequest().permitAll() // Allowed all requests
```

**After:**
```java
.requestMatchers("user-shops/**").authenticated() // Basic auth for user-shops
.anyRequest().authenticated() // Require authentication
```

### Adding @RequirePrivilege

**Before:**
```java
@GetMapping("/users")
public ResponseEntity<DataResponse> getUsers() {
    // No privilege checking
}
```

**After:**
```java
@GetMapping("/users") 
@RequirePrivilege(PrivilegesEnum.VIEW_USERS)
public ResponseEntity<DataResponse> getUsers() {
    // Privilege checking enforced
}
```

## Best Practices

1. **Layer Security**: Use HTTP security for authentication, `@RequirePrivilege` for authorization
2. **Specific Privileges**: Use the most specific privilege required
3. **Document Requirements**: Comment why certain privileges are needed
4. **Test Thoroughly**: Verify both positive and negative access scenarios
5. **Monitor Logs**: Review access denied logs for security insights

## Testing

```java
@Test
@WithMockUser(authorities = {"VIEW_USERS"})
public void testGetUsers_WithPrivilege_Success() {
    // Test successful access with correct privilege
}

@Test
@WithMockUser(authorities = {"OTHER_PRIVILEGE"})
public void testGetUsers_WithoutPrivilege_AccessDenied() {
    // Test access denied with insufficient privileges
}
```

This system provides a robust, type-safe way to handle privilege checking while maintaining the security benefits of Spring Security's HTTP-level protection. 