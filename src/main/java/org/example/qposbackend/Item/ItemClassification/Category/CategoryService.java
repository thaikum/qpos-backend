package org.example.qposbackend.Item.ItemClassification.Category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategoryRepository;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final MainCategoryRepository mainCategoryRepository;
  private final SpringSecurityAuditorAware auditorAware;

  public Category addCategory(AddCategoryRequest request) {
    Category category = new Category();
    if (request.mainCategoryId() != null) {
      category.setMainCategory(mainCategoryRepository.getReferenceById(request.mainCategoryId()));
    }
    return categoryRepository.save(category);
  }

  public List<Category> getCategories() {
    UserShop userShop =
            auditorAware
                    .getCurrentAuditor()
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
    return categoryRepository.findAllByMainCategory_Shop(userShop.getShop());
  }
}
