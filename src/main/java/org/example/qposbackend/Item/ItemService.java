package org.example.qposbackend.Item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.InventoryItem.PriceDetails.Price.Price;
import org.example.qposbackend.InventoryItem.PriceDetails.PriceDetails;
import org.example.qposbackend.InventoryItem.PriceDetails.PricingMode;
import org.example.qposbackend.Item.ItemClassification.Category.Category;
import org.example.qposbackend.Item.ItemClassification.Category.CategoryRepository;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategory;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategoryRepository;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategory;
import org.example.qposbackend.Item.ItemClassification.SubCategory.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.lang.model.type.NullType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemService {
  private final SubCategoryRepository subCategoryRepository;

  @Value("${upload.path}")
  private String uploadPath;

  private final ItemRepository itemRepository;

  private String saveImage(MultipartFile file, Long id) throws IOException {
    String fileExtension =
        Objects.requireNonNull(file.getOriginalFilename())
            .substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    String uniqueFileName = "item" + "_" + id + "_" + fileExtension;

    Path uploadPath = Path.of(this.uploadPath);
    Path filePath = uploadPath.resolve(uniqueFileName);

    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    return uniqueFileName;
  }

  public Item saveItem(Item item, Optional<MultipartFile> imageOpt) throws IOException {
    var subCategory = subCategoryRepository.findById(Long.parseLong(item.getSubCategory()));
    item.setSubCategoryId(subCategory.orElseThrow());
    item.setBarCode(
        Objects.isNull(item.getBarCode())
            ? ""
            : item.getBarCode().trim().isEmpty() ? null : item.getBarCode().trim());
    item = itemRepository.save(item);

    if (imageOpt.isPresent()) {
      MultipartFile file = imageOpt.get();
      if (!file.isEmpty()) {
        String fileName = saveImage(file, item.getId());
        item.setImageUrl(uploadPath + fileName);
      }
    }

    return item;
  }

  public Item updateItem(Long itemId, Item item, Optional<MultipartFile> imageOpt)
      throws IOException {
    Item oldItem =
        itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
    oldItem.setName(Optional.ofNullable(item.getName()).orElse(oldItem.getName()));
    String newBarcode =
        Objects.isNull(item.getBarCode())
            ? ""
            : item.getBarCode().trim().isEmpty() ? null : item.getBarCode().trim();
    oldItem.setBarCode(Optional.ofNullable(newBarcode).orElse(oldItem.getBarCode()));

    if (imageOpt.isPresent()) {
      MultipartFile file = imageOpt.get();
      if (!file.isEmpty()) {
        String fileName = saveImage(file, item.getId());
        item.setImageUrl(uploadPath + fileName);
      }
    }
    SubCategory newSubCategory =
        subCategoryRepository.findById(Long.parseLong(item.getSubCategory())).orElse(null);
    oldItem.setSubCategoryId(
        Optional.ofNullable(newSubCategory).orElse(oldItem.getSubCategoryId()));
    oldItem.setMinimumPerUnit(
        Optional.ofNullable(item.getMinimumPerUnit()).orElse(oldItem.getMinimumPerUnit()));
    oldItem.setUnitOfMeasure(
        Optional.ofNullable(item.getUnitOfMeasure()).orElse(oldItem.getUnitOfMeasure()));
    oldItem.setBrand(Optional.ofNullable(item.getBrand()).orElse(oldItem.getBrand()));
    return itemRepository.save(oldItem);
  }

  public byte[] serveImage(String imageName) throws IOException {
    Path imagePath = Paths.get(uploadPath, imageName); // Adjust the path to your images directory
    Resource resource = new FileSystemResource(imagePath);
    if (!resource.exists()) {
      throw new FileNotFoundException();
    }
    return Files.readAllBytes(imagePath);
  }

  @Transactional
  @Bean("categoryCreator")
  public int categoryObjCreator(
      MainCategoryRepository mainCategoryRepository,
      CategoryRepository categoryRepository,
      SubCategoryRepository subCategoryRepository) {
    List<Item> items = itemRepository.findAll();

    Map<String, MainCategory> mainCategoryMap = new HashMap<>();
    Map<String, Category> categoryMap = new HashMap<>();
    Map<String, SubCategory> subCategoryMap = new HashMap<>();

    for (Item item : items) {
      if (!Objects.isNull(item.getSubCategoryId())) continue;

      String categoryName = item.getCategory();
      String mainCategory = item.getMainCategory();
      String subCategory = item.getSubCategory();

      if (Objects.isNull(mainCategory)) mainCategory = categoryName;

      mainCategory = mainCategory.toUpperCase().trim();
      categoryName = categoryName.toUpperCase().trim();
      subCategory = subCategory.toUpperCase().trim();

      mainCategory = mainCategory.replaceAll("ELECTRON.+", "ELECTRONICS");
      mainCategory = mainCategory.replaceAll("ELECTONICS", "ELECTRONICS");
      mainCategory = mainCategory.replaceAll("ACCESSOR.+", "ACCESSORIES");
      mainCategory = mainCategory.replaceAll("AESSORIES", "ACCESSORIES");
      mainCategory = mainCategory.replaceAll("ELECTRIC.+", "ELECTRICALS");

      categoryName = categoryName.replaceAll("ELECTRON.+", "ELECTRONICS");
      categoryName = categoryName.replaceAll("ELECTONICS", "ELECTRONICS");
      categoryName = categoryName.replaceAll("ACCESSOR.+", "ACCESSORIES");
      categoryName =
          categoryName.replaceAll("AESSORIES|ACESSORIES|ACCESORIES|ACCECCORIES", "ACCESSORIES");
      categoryName = categoryName.replaceAll("ELECTRIC.+", "ELECTRICALS");

      MainCategory mainCategoryObj;
      if (mainCategoryMap.containsKey(mainCategory)) {
        mainCategoryObj = mainCategoryMap.get(mainCategory);
      } else {
        mainCategoryObj =
            mainCategoryRepository
                .findByMainCategoryName(mainCategory)
                .orElse(MainCategory.builder().mainCategoryName(mainCategory).build());

        if (Objects.isNull(mainCategoryObj.getId()))
          mainCategoryObj = mainCategoryRepository.save(mainCategoryObj);
        mainCategoryMap.put(mainCategory, mainCategoryObj);
      }

      Category category;
      if (categoryMap.containsKey(categoryName)) {
        category = categoryMap.get(categoryName);
      } else {
        category =
            categoryRepository
                .findByCategoryNameAndMainCategory_MainCategoryName(categoryName, mainCategory)
                .orElse(
                    Category.builder()
                        .categoryName(categoryName)
                        .mainCategory(mainCategoryObj)
                        .build());

        if (Objects.isNull(category.getId())) category = categoryRepository.save(category);

        categoryMap.put(categoryName, category);
      }

      SubCategory subCategoryObj;
      if (subCategoryMap.containsKey(subCategory)) {
        subCategoryObj = subCategoryMap.get(subCategory);
      } else {
        subCategoryObj =
            subCategoryRepository
                .findBySubCategoryNameAndCategory_CategoryNameAndCategory_MainCategory_MainCategoryName(
                    subCategory, categoryName, mainCategory)
                .orElse(
                    SubCategory.builder().subCategoryName(subCategory).category(category).build());

        if (Objects.isNull(subCategoryObj.getId()))
          subCategoryObj = subCategoryRepository.save(subCategoryObj);

        subCategoryMap.put(subCategory, subCategoryObj);
      }

      item.setSubCategoryId(subCategoryObj);
    }

    itemRepository.saveAll(items);
    return 0;
  }
}
