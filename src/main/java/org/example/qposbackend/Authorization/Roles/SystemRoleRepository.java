package org.example.qposbackend.Authorization.Roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemRoleRepository extends JpaRepository<SystemRole, String> {
}
