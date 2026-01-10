package org.example.qposbackend.InventoryItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findInventoryItemByIdAndShop_id(long iId, long shopId);

    List<InventoryItem> findInventoryItemByShop_IdAndIsDeleted(Long shopId, Boolean isDeleted);

    @Query(nativeQuery = true, value = "update inventory_item set is_deleted = true where id=:id")
    void markDelete(Long id);


    @Query(nativeQuery = true, value = """
            select ii.*
            from inventory_item ii
                     join item i on ii.item_id = i.id
                     join sub_category sc on i.sub_category_id = sc.id
                     join category c on sc.category_id = c.id
            where ii.id in (:itemIds)
               or i.sub_category_id in (:subCategoryIds)
               or sc.category_id in (:categoryIds)
               or c.main_category_id in (:mainCategoryIds)
            
            order by ii.buying_price
            limit 1
            """)
    Optional<InventoryItem> findCheapestItemInInventoryIdsOrItemSubCategoryIdsOrItemCategoryIdsOrItemMainCategoryIds(Iterable<Long> itemIds, Iterable<Long> subCategoryIds, Iterable<Long> categoryIds, Iterable<Long> mainCategoryIds);
}
