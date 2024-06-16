package org.example.qposbackend.Configurations.AdminParameters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminParametersRepository extends JpaRepository<AdminParameters, Integer> {
}
