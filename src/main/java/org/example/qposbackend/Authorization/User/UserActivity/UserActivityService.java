package org.example.qposbackend.Authorization.User.UserActivity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Configurations.AdminParameters.AdminParameters;
import org.example.qposbackend.Configurations.AdminParameters.AdminParametersRepository;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserActivityService {
  private final UserActivityRepository userActivityRepository;
  private final SpringSecurityAuditorAware springSecurityAuditorAware;
  private final AdminParametersRepository adminParametersRepository;

  public void createUserActivity(HttpServletRequest request) {
    AdminParameters adminParameters = adminParametersRepository.findAll().get(0);

    if (Objects.equals(adminParameters.getCheckInIp(), request.getRemoteAddr())) {
      UserActivity userActivity = new UserActivity();
      userActivity.setTimeIn(new Date());
      UserShop userShop =
          springSecurityAuditorAware
              .getCurrentAuditor()
              .orElseThrow(() -> new NoSuchElementException("User not logged in"));
      userActivity.setUserShop(userShop);
      userActivityRepository.save(userActivity);
    } else {
      throw new RuntimeException("Make sure you are at the shop before checking in");
    }
  }

  public void checkOut(HttpServletRequest request) {
    AdminParameters adminParameters = adminParametersRepository.findAll().get(0);

    if (Objects.equals(adminParameters.getCheckOutIp(), request.getRemoteAddr())) {
      UserShop userShop = springSecurityAuditorAware.getCurrentAuditor().get();
      Optional<UserActivity> optionalUserActivity =
          userActivityRepository.findFirstByUserShopIdOrderByTimeInDesc(userShop.getId());
      if (optionalUserActivity.isPresent()) {
        UserActivity userActivity = optionalUserActivity.get();
        userActivity.setTimeOut(new Date());
        userActivityRepository.save(userActivity);
      }
    } else {
      throw new RuntimeException("Make sure you are at the shop before checking out");
    }
  }
}
