package org.example.qposbackend.InventoryItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findInventoryItemByItem_Id(Long id);

    List<InventoryItem> findInventoryItemByIsDeleted(Boolean isDeleted);


    @Query(nativeQuery = true, value = "update inventory_item set is_deleted = true where id=:id")
    void markDelete(Long id);
}
