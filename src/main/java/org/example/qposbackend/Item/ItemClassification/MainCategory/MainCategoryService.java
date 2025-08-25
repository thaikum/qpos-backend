package org.example.qposbackend.Item.ItemClassification.MainCategory;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.example.qposbackend.Security.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MainCategoryService {
  private final MainCategoryRepository mainCategoryRepository;
  private final SpringSecurityAuditorAware auditorAware;

  public MainCategory createMainCategory(MainCategory mainCategory) {

    UserShop userShop =
            auditorAware
                    .getCurrentAuditor()
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
    mainCategory.setShop(userShop.getShop());
    return mainCategoryRepository.save(mainCategory);
  }
 
}
