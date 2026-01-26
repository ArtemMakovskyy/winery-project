package com.winestoreapp.wineryadminui.features.wine;

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

    public WineDto create(WineCreateRequestDto createDto) {
        log.info("Creating wine");
        return wineFeignClient.createWine(createDto);
    }

    public List<WineDto> getAll() {
        return wineFeignClient.getAll();
    }

    public void delete(Long wineId) {
        log.info("Attempting to delete wine with ID: {}", wineId);
        try {
            wineFeignClient.deleteWine(wineId);
            log.info("Successfully deleted wine with ID: {}", wineId);
        } catch (FeignException.NotFound e) {
            log.warn("Action ignored: Wine ID {} not found on backend. Response: {}", wineId, e.contentUTF8());
            throw e;
        } catch (FeignException e) {
            log.error("Backend communication failed for wine ID {}: Status {}", wineId, e.status());
            throw e;
        }
    }
}
