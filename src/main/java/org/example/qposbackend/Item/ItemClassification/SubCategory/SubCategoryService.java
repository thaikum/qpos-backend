package org.example.qposbackend.Item.ItemClassification.SubCategory;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Item.ItemClassification.Category.CategoryRepository;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SubCategoryService {
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final SpringSecurityAuditorAware auditorAware;
    
    public SubCategory addSubCategory(SubCategoryRequest request) {
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        
        var subCategory = new SubCategory();
        subCategory.setSubCategoryName(request.subCategoryName());
        subCategory.setCategory(category);
        
        return subCategoryRepository.save(subCategory);
    }

    public List<SubCategory> getSubCategories(){
        UserShop userShop =
                auditorAware
                        .getCurrentAuditor()
                        .orElseThrow(() -> new NoSuchElementException("User not found"));
        return subCategoryRepository.findAllByCategory_MainCategory_Shop(userShop.getShop());
    }
}
