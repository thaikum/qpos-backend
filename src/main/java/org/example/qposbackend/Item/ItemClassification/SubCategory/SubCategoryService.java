package org.example.qposbackend.Item.ItemClassification.SubCategory;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Item.ItemClassification.Category.CategoryRepository;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class SubCategoryService {
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    
    public SubCategory addSubCategory(SubCategoryRequest request) {
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        
        var subCategory = new SubCategory();
        subCategory.setSubCategoryName(request.subCategoryName());
        subCategory.setCategory(category);
        
        return subCategoryRepository.save(subCategory);
    }
}
