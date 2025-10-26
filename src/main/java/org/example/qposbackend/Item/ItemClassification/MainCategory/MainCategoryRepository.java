package org.example.qposbackend.Item.ItemClassification.MainCategory;

import org.example.qposbackend.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {
    Optional<MainCategory> findByMainCategoryName(String name);
    List<MainCategory> findAllByShop(Shop shop);
}
