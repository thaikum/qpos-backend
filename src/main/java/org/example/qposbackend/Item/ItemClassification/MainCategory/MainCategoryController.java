package org.example.qposbackend.Item.ItemClassification.MainCategory;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("main-categories")
@RequiredArgsConstructor
public class MainCategoryController {
    private final MainCategoryRepository mainCategoryRepository;

    @GetMapping
    public ResponseEntity<DataResponse> getMainCategories(){
        return ResponseEntity.ok(new DataResponse(mainCategoryRepository.findAll(), null));
    }
}
