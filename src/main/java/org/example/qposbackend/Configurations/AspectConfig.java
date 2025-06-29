package org.example.qposbackend.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration to enable AspectJ auto-proxy for AOP functionality.
 * This is required for the @RequirePrivilege annotation to work properly.
 */
@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {
} 