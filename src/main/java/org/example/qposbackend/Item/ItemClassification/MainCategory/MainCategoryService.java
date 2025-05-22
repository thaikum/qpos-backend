package org.example.qposbackend.Item.ItemClassification.MainCategory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MainCategoryService {
  private final MainCategoryRepository mainCategoryRepository;

  public MainCategory createMainCategory(MainCategory mainCategory) {
    return mainCategoryRepository.save(mainCategory);
  }
 
}
