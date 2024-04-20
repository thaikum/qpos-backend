package org.example.qposbackend.Security;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@Configuration
@RequiredArgsConstructor
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class JpaConfig {
    @Bean
    public AuditorAware<User> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }
}
