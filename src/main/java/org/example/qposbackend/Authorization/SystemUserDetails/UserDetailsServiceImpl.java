package org.example.qposbackend.Authorization.SystemUserDetails;

import org.example.qposbackend.Authorization.User.Password.Password;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service

public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public SystemUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findUserByEmail(username);

        if (userOptional.isPresent()) {
            return new SystemUserDetails(userOptional.get());
        } else {
            throw new UsernameNotFoundException("Invalid username of password");
        }
    }

    public String addUser(User user) {
        Password password = Password.builder()
                .password(encoder.encode("12345678"))
        .build();

        user.setPasswords(List.of(password));
        userRepository.save(user);
        return "User Added Successfully";
    }
}
