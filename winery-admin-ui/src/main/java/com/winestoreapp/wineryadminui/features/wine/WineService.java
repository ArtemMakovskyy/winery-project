package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Service
@RequiredArgsConstructor
@Slf4j
public class WineService {

    private final WineFeignClient wineFeignClient;
    private final FeignErrorParser errorParser;
    private final Tracer tracer;

    @Observed(name = "wine.service.create")
    public WineDto create(WineCreateRequestDto createDto) {
        log.info("Creating new wine. Name: {}, VendorCode: {}", createDto.getName(), createDto.getVendorCode());

        tagSpan("wine.vendor_code", createDto.getVendorCode());
        tagSpan("wine.name", createDto.getName());

        try {
            WineDto created = wineFeignClient.createWine(createDto);

            tagSpan("status", "success");
            tagSpan("wine.id", created.getId());

            log.info("Wine created successfully with ID: {}", created.getId());
            return created;
        } catch (FeignException e) {
            String error = errorParser.extractMessage(e);
            log.error("Failed to create wine '{}': {}", createDto.getName(), error);

            tagSpan("status", "error");
            recordError(e);

            throw new RuntimeException(error, e);
        }
    }

    @Observed(name = "wine.service.get_all")
    public List<WineDto> getAll() {
        return wineFeignClient.getAll();
    }

    @Observed(name = "wine.service.delete")
    public void delete(Long wineId) {
        log.info("Deleting wine ID: {}", wineId);
        tagSpan("wine.id", wineId);

        try {
            wineFeignClient.deleteWine(wineId);
            log.info("Wine ID: {} deleted successfully", wineId);
            tagSpan("status", "success");
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.warn("Wine ID {} not found, nothing to delete.", wineId);
                tagSpan("status", "not_found");
            } else {
                String cleanMessage = errorParser.extractMessage(e);
                log.error("Deletion failed for wine ID {}: {}", wineId, cleanMessage, e);

                tagSpan("status", "error");
                recordError(e);

                throw new RuntimeException(cleanMessage, e);
            }
        }
    }

    @Observed(name = "wine.service.update_images")
    public void updateImages(Long id, MultipartFile imageA, MultipartFile imageB) {
        tagSpan("wine.id", id);
        try {
            wineFeignClient.updateWineImages(id, imageA, imageB);
            tagSpan("status", "success");
        } catch (FeignException e) {
            String error = errorParser.extractMessage(e);
            log.error("Failed to update images for wine ID {}: {}", id, error);

            tagSpan("status", "error");
            recordError(e);

            throw new RuntimeException(error, e);
        }
    }

    private void tagSpan(String key, Object value) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, String.valueOf(value));
        }
    }

    private void recordError(Throwable e) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.error(e);
        }
    }
}
