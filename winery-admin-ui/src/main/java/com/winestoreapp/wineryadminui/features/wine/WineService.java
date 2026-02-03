package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WineService {

    private final WineFeignClient wineFeignClient;
    private final FeignErrorParser errorParser;
    private final Tracer tracer;

    @Observed(name = "wine.service.create")
    public WineDto create(WineCreateRequestDto createDto) {
        var span = tracer.currentSpan();
        log.info("Creating new wine. Name: {}, VendorCode: {}", createDto.getName(), createDto.getVendorCode());

        if (span != null) {
            span.tag("wine.vendor_code", createDto.getVendorCode());
            span.tag("wine.name", createDto.getName());
        }

        try {
            WineDto created = wineFeignClient.createWine(createDto);

            if (span != null) {
                span.tag("status", "success");
                span.tag("wine.id", String.valueOf(created.getId()));
            }

            log.info("Wine created successfully with ID: {}", created.getId());
            return created;
        } catch (FeignException e) {
            String error = errorParser.extractMessage(e);
            log.error("Failed to create wine '{}': {}", createDto.getName(), error);
            if (span != null) {
                span.tag("status", "error");
                span.error(e);
            }
            throw new RuntimeException(error, e);
        }
    }

    @Observed(name = "wine.service.get_all")
    public List<WineDto> getAll() {
        return wineFeignClient.getAll();
    }

    @Observed(name = "wine.service.delete")
    public void delete(Long wineId) {
        var span = tracer.currentSpan();
        log.info("Deleting wine ID: {}", wineId);

        if (span != null) {
            span.tag("wine.id", String.valueOf(wineId));
        }

        try {
            wineFeignClient.deleteWine(wineId);
            log.info("Wine ID: {} deleted successfully", wineId);

            if (span != null) {
                span.tag("status", "success");
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.warn("Wine ID {} not found, nothing to delete.", wineId);
                if (span != null) {
                    span.tag("status", "not_found");
                }
            } else {
                String cleanMessage = errorParser.extractMessage(e);
                log.error("Deletion failed for wine ID {}: {}", wineId, cleanMessage, e);
                if (span != null) {
                    span.tag("status", "error");
                    span.error(e);
                }
                throw new RuntimeException(cleanMessage, e);
            }
        }
    }

    @Observed(name = "wine.service.update_images")
    public void updateImages(Long id, MultipartFile imageA, MultipartFile imageB) {
        try {
            wineFeignClient.updateWineImages(id, imageA, imageB);
        } catch (FeignException e) {
            String error = errorParser.extractMessage(e);
            log.error("Failed to update images for wine ID {}: {}", id, error);
            throw new RuntimeException(error, e);
        }
    }

}
