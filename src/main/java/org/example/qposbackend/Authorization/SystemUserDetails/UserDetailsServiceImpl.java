package org.example.qposbackend.Authorization.SystemUserDetails;

import java.util.List;
import java.util.Optional;
import org.example.qposbackend.Authorization.User.Password.Password;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.UserRepository;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Authorization.User.userShop.UserShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder encoder;
  @Autowired private UserShopRepository userShopRepository;

  @Override
  public SystemUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    throw new UnsupportedOperationException("Use loadUserByEmailAndShop instead");
  }

  public SystemUserDetails loadUserByEmailAndShop(String email, String shopCode) {
    UserShop userShop =
        userShopRepository
            .findUserShopByShop_CodeAndUser_email(shopCode, email)
            .orElseThrow(() -> new UsernameNotFoundException("No user in specified shop"));
    return new SystemUserDetails(userShop);
  }

  public String addUser(User user) {
    Password password = Password.builder().password(encoder.encode("12345678")).build();

    user.setPasswords(List.of(password));
    userRepository.save(user);
    return "User Added Successfully";
  }
}
