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
}
