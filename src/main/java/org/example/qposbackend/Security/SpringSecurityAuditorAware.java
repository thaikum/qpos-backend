package org.example.qposbackend.Security;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.core.ApplicationContext;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<User> {
    @Autowired
    private UserRepository userRepository;
    @Override
    public Optional<User> getCurrentAuditor() {
        try {
            Optional<UserDetails> userDetailsOptional = Optional.ofNullable(SecurityContextHolder.getContext())
                    .map(SecurityContext::getAuthentication)
                    .filter(Authentication::isAuthenticated)
                    .map(Authentication::getPrincipal)
                    .map(UserDetails.class::cast);

            if(userDetailsOptional.isPresent()){
                return userRepository.findUserByEmail(userDetailsOptional.get().getUsername());
            }else{
                return userRepository.findUserByEmail("no-reply@phoxac.com");
            }
        }catch (Exception ex){
            return Optional.empty();
        }
    }
}
