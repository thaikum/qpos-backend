package org.example.qposbackend.Item.ItemClassification.MainCategory;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("main-categories")
@RequiredArgsConstructor
public class MainCategoryController {
    private final MainCategoryRepository mainCategoryRepository;
    private final MainCategoryService mainCategoryService;

    @GetMapping
    public ResponseEntity<DataResponse> getMainCategories(){
        return ResponseEntity.ok(new DataResponse(mainCategoryService.getMainCategories(), null));
    }
    
    @PostMapping
    public ResponseEntity<DataResponse> createMainCategory(@RequestBody MainCategory mainCategory) {
        try {
            MainCategory savedCategory = mainCategoryService.createMainCategory(mainCategory);
            return ResponseEntity.ok(new DataResponse(savedCategory, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DataResponse(null, e.getMessage()));
        }
    }
}
