package org.example.qposbackend.Item.ItemClassification.SubCategory;

import lombok.AllArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sub-categories")
@AllArgsConstructor
public class SubCategoryController {
    private final SubCategoryRepository subCategoryRepository;

    @GetMapping
    public ResponseEntity<DataResponse> getSubCategories(){
        return ResponseEntity.ok(new DataResponse(subCategoryRepository.findAll(), null));
    }
}
