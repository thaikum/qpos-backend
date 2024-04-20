package org.example.qposbackend.Authorization.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.Roles.SystemRoleRepository;
import org.example.qposbackend.Authorization.User.Password.Password;
import org.example.qposbackend.DTOs.PasswordChange;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SystemRoleRepository systemRoleRepository;

    public void updatePassword(PasswordChange passwordChange) {
        Optional<UserDetails> userDetailsOptional = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(UserDetails.class::cast);

        if (userDetailsOptional.isPresent()) {
            Optional<User> user = userRepository.findUserByEmail(userDetailsOptional.get().getUsername());

            if (user.isPresent()) {
                List<Password> passwords = user.get().getPasswords();
                if (passwords.size() == 12) {
                    passwords.remove(0);
                }
                passwords.add(
                        Password.builder()
                                .password(passwordEncoder.encode(passwordChange.password()))
                                .build()
                );
                User newUser = user.get();
                newUser.setPasswords(passwords);
                userRepository.save(newUser);
            } else {
                throw new RuntimeException("User not found");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Bean
    public void createInitialUser(){
        Optional<SystemRole> optionalSystemRole = systemRoleRepository.findById("ADMIN");

        if(optionalSystemRole.isPresent() && userRepository.findAll().isEmpty()){
            SystemRole systemRole = optionalSystemRole.get();

            User user = User.builder()
                    .email("fredthaiku@gmail.com")
                    .enabled(true)
                    .firstName("Fredrick")
                    .lastName("Thaiku")
                    .idType(IdType.NATIONAL_ID)
                    .passwords(List.of(Password.builder()
                            .password(passwordEncoder.encode("@Fredsystem5647"))
                            .build()))
                    .role(systemRole)
                .build();

            userRepository.save(user);
            log.info("Initial user created");
        }
    }
}
