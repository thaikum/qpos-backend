package org.example.qposbackend.Authorization.User.UserActivity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUserId(Long userId);
    Optional<UserActivity> findFirstByUserIdOrderByTimeInDesc(Long userId);
}
