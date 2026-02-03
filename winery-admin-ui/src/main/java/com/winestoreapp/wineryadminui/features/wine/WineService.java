package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import feign.FeignException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WineService {

    private final WineFeignClient wineFeignClient;
    private final FeignErrorParser errorParser;

    public WineDto create(WineCreateRequestDto createDto) {
        log.info("Action: Creating new wine. Name: {}, VendorCode: {}", createDto.getName(), createDto.getVendorCode());
        try {
            WineDto created = wineFeignClient.createWine(createDto);
            log.info("Wine created successfully with ID: {}", created.getId());
            return created;
        } catch (FeignException e) {
            String error = errorParser.extractMessage(e);
            log.error("Failed to create wine '{}': {}", createDto.getName(), error);
            throw new RuntimeException(error);
        }
    }

    public List<WineDto> getAll() {
        return wineFeignClient.getAll();
    }

    public void delete(Long wineId) {
        log.info("Action: Deleting wine ID: {}", wineId);
        try {
            wineFeignClient.deleteWine(wineId);
            log.info("Wine ID: {} deleted successfully", wineId);
        } catch (FeignException e) {
            String cleanMessage = errorParser.extractMessage(e);
            log.warn("Deletion failed for wine ID {}: {}", wineId, cleanMessage);
            throw new RuntimeException(cleanMessage);
        }
    }
}