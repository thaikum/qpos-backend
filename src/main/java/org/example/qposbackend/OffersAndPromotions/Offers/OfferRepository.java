package org.example.qposbackend.OffersAndPromotions.Offers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Query(nativeQuery = true, value = """
                SELECT o.* FROM offer o
                JOIN offer_items oi ON o.id = oi.offer_id
                WHERE o.is_active = true AND oi.items_id IN (:ids)
            """)
    List<Offer> findAllActiveOffersByItems(@Param("ids") List<Long> ids);


    @Query(nativeQuery = true, value = """
            SELECT o.*
            FROM offer o
                     LEFT JOIN offer_items oi ON o.id = oi.offer_id
                     LEFT JOIN offer_categories oc ON o.id = oc.offer_id
                     LEFT JOIN offer_sub_categories osc ON o.id = osc.offer_id
                     LEFT JOIN offer_main_categories omc ON o.id = omc.offer_id
            
            WHERE o.is_active = TRUE
              AND o.start_date <= CURRENT_TIMESTAMP()
              AND o.end_date > CURRENT_TIMESTAMP()
              AND (o.effect_on = 'ALL' -- where the offer is applicable to all
                OR (
                       (oi.items_id IN (:itemIds))
                           OR (oc.categories_id in (:categoryIds) AND o.effect_on = 'CATEGORIES')
                           OR (osc.sub_categories_id IN (:subCategoryIds) AND o.effect_on = 'SUB_CATEGORIES')
                           OR (omc.main_categories_id IN (:mainCategoryIds) AND o.effect_on = 'MAIN_CATEGORIES')
                       ))

            """)
    List<Offer> findAllActiveOffersByItemsIdsAndCategoriesIdsAndMainCategoriesIdsAndSubCategoriesIds(Iterable<Long> itemIds, Iterable<Long> categoryIds, Iterable<Long> mainCategoryIds, Iterable<Long> subCategoryIds);

    @Query(nativeQuery = true, value = """
            SELECT o.*
            FROM offer o
                     LEFT JOIN offer_items oi ON o.id = oi.offer_id
                     LEFT JOIN offer_categories oc ON o.id = oc.offer_id
                     LEFT JOIN offer_sub_categories osc ON o.id = osc.offer_id
                     LEFT JOIN offer_main_categories omc ON o.id = omc.offer_id
            
            WHERE o.is_active = TRUE
              AND o.start_date <= CURRENT_TIMESTAMP()
              AND o.end_date > CURRENT_TIMESTAMP()
              AND o.discount_type = 'PERCENTAGE'
              AND o.discount_allowed = 100
              AND (o.effect_on = 'ALL' -- where the offer is applicable to all
                OR NOT (
                       (oi.items_id IN (:itemIds))
                           OR (oc.categories_id in (:categoryIds) AND o.effect_on = 'CATEGORIES')
                           OR (osc.sub_categories_id IN (:subCategoryIds) AND o.effect_on = 'SUB_CATEGORIES')
                           OR (omc.main_categories_id IN (:mainCategoryIds) AND o.effect_on = 'MAIN_CATEGORIES')
                       ))
            
            """)
    List<Offer> findAllActiveFullyFreeOffersNotInItemsIdsAndNotInCategoriesIdsAndNotInMainCategoriesIdsAndNotInSubCategoriesIds(Iterable<Long> itemIds, Iterable<Long> categoryIds, Iterable<Long> mainCategoryIds, Iterable<Long> subCategoryIds);
}
