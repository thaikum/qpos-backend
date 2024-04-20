package org.example.qposbackend.Item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

        if(imageOpt.isPresent()){
            MultipartFile file = imageOpt.get();
            if (!file.isEmpty()) {
                String fileName = saveImage(file, item.getId());
                item.setImageUrl(uploadPath + fileName);
            }
        }

        return item;
    }

    public byte[] serveImage(String imageName) throws IOException{
        Path imagePath = Paths.get(uploadPath, imageName); // Adjust the path to your images directory
        Resource resource = new FileSystemResource(imagePath);
        if (!resource.exists()) {
            throw new FileNotFoundException();
        }
        return Files.readAllBytes(imagePath);
    }
}
