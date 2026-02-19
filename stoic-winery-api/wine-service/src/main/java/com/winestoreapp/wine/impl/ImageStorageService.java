package com.winestoreapp.wine.impl;

import com.winestoreapp.common.observability.ObservationContextualNames;
import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.common.observability.ObservationTags;
import com.winestoreapp.common.observability.SpanTagger;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageStorageService {

    private final SpanTagger spanTagger;
    @Value("${image.save.path}")
    private String imageSavePath;

    @Observed(name = ObservationNames.IMAGE_STORAGE, contextualName = ObservationContextualNames.UPDATE_BY_NAME)
    public String saveImage(String originalName, String suffix, MultipartFile file) {
        try {
            Path uploadPath = Path.of(imageSavePath);
            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = generateUniqueFileName(suffix, Objects.requireNonNull(originalName));
            Path filePath = uploadPath.resolve(fileName);

            spanTagger.tag(ObservationTags.FILE_NAME, fileName);
            spanTagger.tag(ObservationTags.FILE_SIZE, file.getSize());

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Image successfully saved: {}", filePath);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error saving image: " + e.getMessage(), e);
        }
    }

    @Observed(name = ObservationNames.IMAGE_STORAGE, contextualName = ObservationContextualNames.DELETE_BY_ID)
    public void deleteImage(String pictureLink) {
        if (pictureLink == null || pictureLink.isEmpty()) return;

        try {
            String fileName = pictureLink.substring(pictureLink.lastIndexOf("/") + 1);
            Path filePath = Paths.get(imageSavePath + fileName);

            spanTagger.tag(ObservationTags.FILE_PATH, filePath.toString());

            if (Files.deleteIfExists(filePath)) {
                log.info("Old image deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", pictureLink, e);
            throw new RuntimeException("Error deleting image", e);
        }
    }

    private String generateUniqueFileName(String suffix, String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);
        return baseName + "_" + suffix + "_" + System.currentTimeMillis() + extension;
    }

}
