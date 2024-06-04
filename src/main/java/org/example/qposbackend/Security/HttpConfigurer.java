package org.example.qposbackend.Security;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.example.qposbackend.Authorization.SystemUserDetails.UserDetailsServiceImpl;
import org.example.qposbackend.Security.Jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.access.AccessDeniedException;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class HttpConfigurer {
    private final JwtAuthFilter authFilter;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        //OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        // ================================ SALES ===============================================
                        .requestMatchers(HttpMethod.POST, "order").hasAuthority(PrivilegesEnum.MAKE_SALE.name())
                        .requestMatchers(HttpMethod.POST, "order/get-by-range").hasAuthority(PrivilegesEnum.VIEW_HISTORICAL_SALES.name())
                        .requestMatchers(HttpMethod.POST, "order/get-by-range").denyAll()

                        //================================= INVENTORY ===========================================
                        .requestMatchers(HttpMethod.GET, "inventory").hasAuthority(PrivilegesEnum.VIEW_INVENTORY.name())
                        .requestMatchers(HttpMethod.POST, "inventory").hasAuthority(PrivilegesEnum.ADD_INVENTORY_ITEM.name())
                        .requestMatchers(HttpMethod.PUT, "inventory/delete").hasAuthority(PrivilegesEnum.DELETE_INVENTORY_ITEM.name())

                        //================================= ROLES ===============================================
                        .requestMatchers(HttpMethod.GET, "roles", "privileges").hasAuthority(PrivilegesEnum.VIEW_ROLES.name())
                        .requestMatchers(HttpMethod.POST, "roles").hasAuthority(PrivilegesEnum.ADD_ROLE.name())

                        // ================================ STOCK ===============================================
                        .requestMatchers(HttpMethod.GET, "stock").hasAuthority(PrivilegesEnum.VIEW_STOCK.name())
                        .requestMatchers(HttpMethod.POST, "stock").hasAuthority(PrivilegesEnum.ADD_STOCK.name())

                        // ================================== USER ==============================================
                        .requestMatchers(HttpMethod.POST, "users").hasAuthority(PrivilegesEnum.ADD_USER.name())
                        .requestMatchers(HttpMethod.GET, "users").hasAuthority(PrivilegesEnum.VIEW_USERS.name())
                        .requestMatchers(HttpMethod.POST, "users/change-password").authenticated()

                        // =============================== RESOURCES =============================================
                        .requestMatchers(HttpMethod.GET, "item/**").permitAll()

                        // =============================== ACCOUNTING ============================================
                        .requestMatchers(HttpMethod.POST, "accounts").hasAuthority(PrivilegesEnum.CREATE_ACCOUNT.name())
                        .requestMatchers(HttpMethod.GET, "accounts").hasAuthority(PrivilegesEnum.VIEW_ACCOUNTS.name())

                        // ================================= OTHERS ===============================================
                        .requestMatchers("/users/login").permitAll()
                        .anyRequest().denyAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (accessDeniedException != null) {
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                                response.getWriter().write("Access forbidden");
                            } else {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.getWriter().write("Unauthorized");
                            }
                        })

                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
