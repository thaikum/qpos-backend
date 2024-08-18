package org.example.qposbackend.Item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;


@RestController
@RequestMapping("item")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("images/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) {
        try{
            byte [] imageBytes = itemService.serveImage(imageName);
            return ResponseEntity.ok().body(imageBytes);
        }catch (Exception exception){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

    }
}
