package org.example.qposbackend.Item.ItemClassification.Category;

import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryNameAndMainCategory_MainCategoryName(String categoryName, String mainCategory);
}
