package com.winestoreapp.wine.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageStorageService {

    @Value("${image.save.path}")
    private String imageSavePath;

    private final Tracer tracer;

    @Observed(name = "image.storage", contextualName = "save-image")
    public String saveImage(String originalName, String suffix, MultipartFile file) {
        try {
            Path uploadPath = Path.of(imageSavePath);
            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = generateUniqueFileName(suffix, Objects.requireNonNull(originalName));
            Path filePath = uploadPath.resolve(fileName);

            if (tracer.currentSpan() != null) {
                tracer.currentSpan().tag("file.name", fileName);
                tracer.currentSpan().tag("file.size", String.valueOf(file.getSize()));
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Image successfully saved: {}", filePath);
            return fileName;
        } catch (IOException e) {
            log.error("Failed to save image: {}", originalName, e);
            throw new RuntimeException("Error saving image: " + e.getMessage(), e);
        }
    }

    @Observed(name = "image.storage", contextualName = "delete-image")
    public void deleteImage(String pictureLink) {
        if (pictureLink == null || pictureLink.isEmpty()) return;

        try {
            String fileName = pictureLink.substring(pictureLink.lastIndexOf("/") + 1);
            Path filePath = Paths.get(imageSavePath + fileName);

            if (tracer.currentSpan() != null) {
                tracer.currentSpan().tag("file.path", filePath.toString());
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Old image deleted: {}", filePath);
            } else {
                log.debug("Image not found for deletion: {}", filePath);
            }
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