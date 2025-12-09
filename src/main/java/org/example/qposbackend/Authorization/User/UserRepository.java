package org.example.qposbackend.Authorization.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findUserByEmail(String email);

  @Query(
          nativeQuery = true,
          value = "SELECT * FROM system_user WHERE email = :value OR id_number = :value")
  Optional<User> findUserByIdNumberOrEmail(String value);
}
