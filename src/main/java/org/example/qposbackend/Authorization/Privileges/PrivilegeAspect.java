package org.example.qposbackend.Authorization.Privileges;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * AOP Aspect that handles @RequirePrivilege annotation.
 * This aspect intercepts method calls annotated with @RequirePrivilege 
 * and performs privilege checking before allowing the method to execute.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PrivilegeAspect {

    private final PrivilegeChecker privilegeChecker;

    /**
     * Around advice that intercepts methods annotated with @RequirePrivilege.
     * Checks user privileges before allowing method execution.
     */
    @Around("@annotation(requirePrivilege)")
    public Object checkPrivilege(ProceedingJoinPoint joinPoint, RequirePrivilege requirePrivilege) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Access denied: User not authenticated for method: {}", 
                    joinPoint.getSignature().toShortString());
            throw new AccessDeniedException("Authentication required");
        }

        PrivilegesEnum[] requiredPrivileges = requirePrivilege.value();
        boolean requireAll = requirePrivilege.requireAll();
        boolean hasAccess;

        if (requireAll) {
            hasAccess = privilegeChecker.hasAllPrivileges(authentication, requiredPrivileges);
        } else {
            hasAccess = privilegeChecker.hasAnyPrivilege(authentication, requiredPrivileges);
        }

        if (!hasAccess) {
            String methodName = joinPoint.getSignature().toShortString();
            String username = authentication.getName();
            log.warn("Access denied: User '{}' lacks required privileges for method: {}. Required: {}, RequireAll: {}", 
                    username, methodName, java.util.Arrays.toString(requiredPrivileges), requireAll);
            throw new AccessDeniedException("Insufficient privileges to access this resource");
        }

        log.debug("Access granted: User '{}' has required privileges for method: {}", 
                authentication.getName(), joinPoint.getSignature().toShortString());
        
        return joinPoint.proceed();
    }

    /**
     * Around advice for class-level @RequirePrivilege annotations.
     * Checks all methods in a class if the class is annotated with @RequirePrivilege.
     */
    @Around("@within(requirePrivilege)")
    public Object checkClassPrivilege(ProceedingJoinPoint joinPoint, RequirePrivilege requirePrivilege) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        if (method.isAnnotationPresent(RequirePrivilege.class)) {
            return joinPoint.proceed();
        }
        
        return checkPrivilege(joinPoint, requirePrivilege);
    }
} 