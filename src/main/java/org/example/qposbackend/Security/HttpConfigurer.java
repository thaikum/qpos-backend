package org.example.qposbackend.Security;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.example.qposbackend.Authorization.SystemUserDetails.UserDetailsServiceImpl;
import org.example.qposbackend.Security.Jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
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
@DependsOn("corsConfig")
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
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((requests) -> requests
                        //OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        // ================================ SALES ===============================================
                        .requestMatchers(HttpMethod.POST, "order").hasAuthority(PrivilegesEnum.MAKE_SALE.name())
                        .requestMatchers(HttpMethod.POST, "order/get-by-range").hasAuthority(PrivilegesEnum.VIEW_HISTORICAL_SALES.name())
                        .requestMatchers(HttpMethod.POST, "order/get-by-range").denyAll()
                        .requestMatchers(HttpMethod.POST, "order/return-item").hasAnyAuthority(PrivilegesEnum.HANDLE_RETURNED_GOODS.name())

                        // ================================ EOD =================================================
                        .requestMatchers(HttpMethod.POST, "eod").hasAnyAuthority(PrivilegesEnum.CLOSE_DAY_BOOKS.name())
                        .requestMatchers(HttpMethod.POST, "eod/fetch-by-range").hasAnyAuthority(PrivilegesEnum.CLOSE_DAY_BOOKS.name())

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
                        .requestMatchers(HttpMethod.PUT, "accounts").hasAuthority(PrivilegesEnum.UPDATE_ACCOUNT.name())

                        // =============================== TRANSACTIONS ==========================================
                        .requestMatchers(HttpMethod.POST, "transactions").hasAuthority(PrivilegesEnum.POST_TRANSACTION.name())
                        .requestMatchers(HttpMethod.GET, "transactions").hasAuthority(PrivilegesEnum.VIEW_TRANSACTIONS.name())
                        .requestMatchers(HttpMethod.PUT, "transactions/verify").hasAuthority(PrivilegesEnum.VERIFY_TRANSACTION.name())
                        .requestMatchers(HttpMethod.POST, "transactions/by-range/{status}").hasAuthority(PrivilegesEnum.VIEW_TRANSACTIONS.name())

                        // =============================== USER ACTIVITY ==========================================
                        .requestMatchers("user-activity/{user-id}", "user-activity/check-in", "user-activity/user-is-checked-in").authenticated()

                        //================================== ADMIN PARAMETERS ====================================
                        .requestMatchers(HttpMethod.GET, "admin-parameters").hasAnyAuthority(PrivilegesEnum.VIEW_ADMIN_PARAMETERS.name())
                        .requestMatchers(HttpMethod.PUT, "admin-parameters").hasAnyAuthority(PrivilegesEnum.UPDATE_ADMIN_PARAMETERS.name())

                        //================================== REPORTS ================================================
                        .requestMatchers("reports/profit_and_loss").hasAnyAuthority(PrivilegesEnum.VIEW_PROFIT_AND_LOSS_REPORT.name())
                        .requestMatchers("reports/account_statement").hasAnyAuthority(PrivilegesEnum.VIEW_ACCOUNT_STATEMENT_REPORT.name())
                        .requestMatchers("reports/restocking_report").hasAnyAuthority(PrivilegesEnum.VIEW_RESTOCKING_ESTIMATES.name())

                        //=================================== OFFERS =================================================
                        .requestMatchers(HttpMethod.POST, "/offers").hasAnyAuthority(PrivilegesEnum.CREATE_OFFER.name())
                        .requestMatchers(HttpMethod.GET, "offers").hasAnyAuthority(PrivilegesEnum.MAKE_SALE.name(), PrivilegesEnum.CREATE_OFFER.name(), PrivilegesEnum.VIEW_OFFER.name())
                        .requestMatchers(HttpMethod.POST, "offers/get-offers_on_order").hasAnyAuthority(PrivilegesEnum.MAKE_SALE.name())

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
