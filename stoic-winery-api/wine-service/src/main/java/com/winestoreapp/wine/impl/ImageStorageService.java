package com.winestoreapp.wine.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ImageStorageService {

    @Value("${image.save.path:src/main/resources/static/images/wine/}")
    private String imageSavePath;

    public String saveImage(String originalName, String suffix, MultipartFile file) {
        try {
            Path uploadPath = Path.of(imageSavePath);
            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = generateUniqueFileName(suffix, Objects.requireNonNull(originalName));
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Image successfully saved: {}", filePath);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error saving image: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String pictureLink) {
        if (pictureLink == null || pictureLink.isEmpty()) return;

        try {
            String fileName = pictureLink.substring(pictureLink.lastIndexOf("/") + 1);
            Path filePath = Paths.get(imageSavePath + fileName);
            Files.deleteIfExists(filePath);
            log.info("Old image deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", pictureLink, e);
        }
    }

    private String generateUniqueFileName(String suffix, String fileName) {
        if (fileName == null) {
            fileName = "image.jpg";
        }
        int dotIndex = fileName.lastIndexOf(".");
        String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);
        return baseName + "_" + suffix + "_" + System.currentTimeMillis() + extension;
    }
}
