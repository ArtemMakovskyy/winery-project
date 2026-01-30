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
        log.info("Creating wine");
        try {
            return wineFeignClient.createWine(createDto);
        } catch (FeignException e) {
            throw new RuntimeException(errorParser.extractMessage(e));
        }
    }

    public List<WineDto> getAll() {
        return wineFeignClient.getAll();
    }

    public void delete(Long wineId) {
        log.info("Attempting to delete wine with ID: {}", wineId);
        try {
            wineFeignClient.deleteWine(wineId);
            log.info("Successfully deleted wine with ID: {}", wineId);
        } catch (FeignException e) {
            String cleanMessage = errorParser.extractMessage(e);
            log.warn("Action failed for wine ID {}: {}", wineId, cleanMessage);
            throw new RuntimeException(cleanMessage);
        }
    }
}