package org.example.qposbackend.EOD;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EODRepository extends JpaRepository<EOD, Long> {
    @Query(nativeQuery = true, value = "select * from eod order by date desc limit 1")
    Optional<EOD> findLastEOD();
}
