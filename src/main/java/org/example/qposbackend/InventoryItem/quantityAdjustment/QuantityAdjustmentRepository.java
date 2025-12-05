package org.example.qposbackend.InventoryItem.quantityAdjustment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuantityAdjustmentRepository extends JpaRepository<QuantityAdjustment, Long> {
}
