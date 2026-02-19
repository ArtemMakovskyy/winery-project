package com.winestoreapp.wine.impl;

import com.winestoreapp.common.observability.SpanTagger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;
    private ImageStorageService imageStorageService;
    @MockBean
    private SpanTagger spanTagger;

    @BeforeEach
    void setUp() {
        spanTagger = Mockito.mock(SpanTagger.class);
        imageStorageService = new ImageStorageService(spanTagger);
        ReflectionTestUtils.setField(imageStorageService, "imageSavePath", tempDir.toString() + "/");
    }

    @Test
    void saveImage_ValidFile_ShouldSaveAndReturnFileName() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "imageA", "test.jpg", "image/jpeg", "content".getBytes());

        String savedFileName = imageStorageService.saveImage("test.jpg", "a", file);

        assertThat(savedFileName).contains("test_a_");
        assertThat(Files.exists(tempDir.resolve(savedFileName))).isTrue();
    }

    @Test
    void deleteImage_ExistingFile_ShouldRemoveFile() throws IOException {
        String fileName = "to_delete.jpg";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, "data".getBytes());

        imageStorageService.deleteImage("api/images/wine/" + fileName);

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void deleteImage_NonExistingFile_ShouldNotThrowException() {
        imageStorageService.deleteImage("api/images/wine/non_existent.jpg");
    }
}