package org.example.qposbackend.Item.ItemClassification.Category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategoryRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final MainCategoryRepository mainCategoryRepository;

  public Category addCategory(AddCategoryRequest request) {
    Category category = new Category();
    category.setCategoryName(request.categoryName());
    if (request.mainCategoryId() != null) {
      category.setMainCategory(mainCategoryRepository.getReferenceById(request.mainCategoryId()));
    }
    return categoryRepository.save(category);
  }
}
