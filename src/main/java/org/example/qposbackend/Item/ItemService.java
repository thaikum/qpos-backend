package org.example.qposbackend.Item;

import jakarta.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    String uniqueFileName = "item" + "_" + id + "_." + fileExtension;

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

    item = itemRepository.save(item);
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
      log.info("Image is available");
      MultipartFile file = imageOpt.get();
      if (!file.isEmpty()) {
        log.info("Image is not empty");
        String fileName = saveImage(file, item.getId());
        oldItem.setImageUrl(uploadPath + fileName);
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
}
