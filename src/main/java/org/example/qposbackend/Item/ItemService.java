package org.example.qposbackend.Item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    @Value("${upload.path}")
    private String uploadPath;

    private final ItemRepository itemRepository;

    private String saveImage(MultipartFile file, Long id) throws IOException {
        String fileExtension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1);
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

    public byte[] serveImage(String imageName) throws IOException {
        Path imagePath = Paths.get(uploadPath, imageName); // Adjust the path to your images directory
        Resource resource = new FileSystemResource(imagePath);
        if (!resource.exists()) {
            throw new FileNotFoundException();
        }
        return Files.readAllBytes(imagePath);
    }


    @Transactional
    @Bean
    public int categoryObjCreator(MainCategoryRepository mainCategoryRepository, CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository) {
        List<Item> items = itemRepository.findAll();

        Map<String, MainCategory> mainCategoryMap = new HashMap<>();
        Map<String, Category> categoryMap = new HashMap<>();
        Map<String, SubCategory> subCategoryMap = new HashMap<>();

        for (Item item : items) {
            if (!Objects.isNull(item.getSubCategoryId()))
                continue;

            String categoryName = item.getCategory();
            String mainCategory = item.getMainCategory();
            String subCategory = item.getSubCategory();

            if (Objects.isNull(mainCategory))
                mainCategory = categoryName;

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
            categoryName = categoryName.replaceAll("AESSORIES|ACESSORIES|ACCESORIES|ACCECCORIES", "ACCESSORIES");
            categoryName = categoryName.replaceAll("ELECTRIC.+", "ELECTRICALS");


            MainCategory mainCategoryObj;
            if (mainCategoryMap.containsKey(mainCategory)) {
                mainCategoryObj = mainCategoryMap.get(mainCategory);
            } else {
                mainCategoryObj = MainCategory.builder().mainCategoryName(mainCategory).build();
                mainCategoryObj = mainCategoryRepository.save(mainCategoryObj);
                mainCategoryMap.put(mainCategory, mainCategoryObj);
            }


            Category category;
            if (categoryMap.containsKey(categoryName)) {
                category = categoryMap.get(categoryName);
            } else {
                category = Category.builder()
                        .categoryName(categoryName)
                        .mainCategory(mainCategoryObj)
                        .build();
                category = categoryRepository.save(category);
                categoryMap.put(categoryName, category);
            }

            SubCategory subCategoryObj;
            if (subCategoryMap.containsKey(subCategory)) {
                subCategoryObj = subCategoryMap.get(subCategory);
            }else {
                subCategoryObj = SubCategory.builder()
                        .subCategoryName(subCategory)
                        .category(category)
                        .build();
                subCategoryObj = subCategoryRepository.save(subCategoryObj);
                subCategoryMap.put(subCategory, subCategoryObj);
            }

            item.setSubCategoryId(subCategoryObj);
        }

        itemRepository.saveAll(items);
        return 0;
    }
}
