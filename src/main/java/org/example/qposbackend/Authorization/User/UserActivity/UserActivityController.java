package org.example.qposbackend.Authorization.User.UserActivity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("user-activity")
@RequiredArgsConstructor
public class UserActivityController {
    private final UserActivityRepository userActivityRepository;
    private final UserActivityService userActivityService;
    private final SpringSecurityAuditorAware springSecurityAuditorAware;

    @GetMapping("{user_id}")
    public ResponseEntity<DataResponse> getUserActivity(@PathVariable("user_id") Long userId) {
        try{
            return ResponseEntity.ok(new DataResponse(userActivityRepository.findByUserId(userId), null));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DataResponse(null, e.getMessage()));
        }
    }

    @GetMapping("user-is-checked-in")
    public ResponseEntity<MessageResponse> getLastSession() {
        try{
            long userId = springSecurityAuditorAware.getCurrentAuditor().get().getId();
            Optional<UserActivity> activity = userActivityRepository.findFirstByUserIdOrderByTimeInDesc(userId);

            if(activity.isPresent() && Objects.isNull(activity.get().getTimeOut())){
                return ResponseEntity.ok(new MessageResponse("yes"));
            }else{
                return ResponseEntity.ok(new MessageResponse("no"));
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("check-in")
    public ResponseEntity<MessageResponse> createUserActivity(HttpServletRequest request) {
        try {
            userActivityService.createUserActivity(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("You have successfully checked in"));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("check-out")
    public ResponseEntity<MessageResponse> checkOut(){
        userActivityService.checkOut();
        return ResponseEntity.ok(new MessageResponse("You have successfully checked out"));
    }
}
