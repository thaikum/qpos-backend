package org.example.qposbackend.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    @Modifying
    @Query("UPDATE Shop set deleted = true where code = :code")
    Shop deleteShopByCode(String code);

    Optional<Shop> findShopByCode(String code);
}
