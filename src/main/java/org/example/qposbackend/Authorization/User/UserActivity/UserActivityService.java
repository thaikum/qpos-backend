package org.example.qposbackend.Authorization.User.UserActivity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Configurations.AdminParameters.AdminParameters;
import org.example.qposbackend.Configurations.AdminParameters.AdminParametersRepository;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;

import java.util.Date;
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

        if(Objects.equals(adminParameters.getCheckInIp(), request.getRemoteAddr())) {
            UserActivity userActivity = new UserActivity();
            User user = springSecurityAuditorAware.getCurrentAuditor().get();
            userActivity.setUser(user);
            userActivityRepository.save(userActivity);
        }else{
            throw new RuntimeException("Make sure you are at the shop before checking in");
        }


    }

    public void checkOut() {
        User user = springSecurityAuditorAware.getCurrentAuditor().get();
        Optional<UserActivity> optionalUserActivity = userActivityRepository.findFirstByUserIdOrderByTimeInDesc(user.getId());
        if(optionalUserActivity.isPresent()) {
            UserActivity userActivity = optionalUserActivity.get();
            userActivity.setTimeOut(new Date());
            userActivityRepository.save(userActivity);
        }
    }
}
