package org.example.qposbackend.Item.ItemClassification.SubCategory;

import lombok.AllArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sub-categories")
@AllArgsConstructor
public class SubCategoryController {
    private final SubCategoryRepository subCategoryRepository;
    private final SubCategoryService subCategoryService;

    @GetMapping
    public ResponseEntity<DataResponse> getSubCategories(){
        return ResponseEntity.ok(new DataResponse(subCategoryRepository.findAll(), null));
    }
    
    @PostMapping
    public ResponseEntity<DataResponse> createSubCategory(@RequestBody SubCategoryRequest request) {
        try {
            return ResponseEntity.ok(new DataResponse(subCategoryService.addSubCategory(request), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DataResponse(null, e.getMessage()));
        }
    }
    
}
