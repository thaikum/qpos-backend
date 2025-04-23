package org.example.qposbackend.Item.ItemClassification.SubCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    Optional<SubCategory> findBySubCategoryNameAndCategory_CategoryNameAndCategory_MainCategory_MainCategoryName(String subCategory, String category, String mainCategory);
}
