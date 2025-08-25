package org.example.qposbackend.Item.ItemClassification.Category;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<DataResponse> getCategory(){
        return ResponseEntity.ok(new DataResponse(categoryService.getCategories(), null));
    }
    
    @PostMapping
    public ResponseEntity<DataResponse> addCategory(@RequestBody AddCategoryRequest request) {
        try {
            Category category = categoryService.addCategory(request);
            return ResponseEntity.ok(new DataResponse(category, null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new DataResponse(null, e.getMessage()));
        }
    }
}
