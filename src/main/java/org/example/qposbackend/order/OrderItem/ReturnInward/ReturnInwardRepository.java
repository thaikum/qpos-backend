package org.example.qposbackend.order.orderItem.ReturnInward;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnInwardRepository extends JpaRepository<ReturnInward, Long> {
}
