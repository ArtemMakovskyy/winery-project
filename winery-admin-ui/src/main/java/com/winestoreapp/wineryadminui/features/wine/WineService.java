package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class WineService {

    private final WineFeignClient wineFeignClient;
    private final SpanTagger spanTagger;

    @Observed(name = ObservationNames.WINE_CREATE)
    public WineDto create(WineCreateRequestDto createDto) {
        spanTagger.tag(ObservationTags.VENDOR_CODE, createDto.getVendorCode());
        spanTagger.tag(ObservationTags.WINE_NAME, createDto.getName());
        return wineFeignClient.createWine(createDto);
    }

    @Observed(name = ObservationNames.WINE_FIND_ALL)
    public List<WineDto> getAll() {
        return wineFeignClient.getAll();
    }

    @Observed(name = ObservationNames.WINE_DELETE)
    public void delete(Long wineId) {
        spanTagger.tag(ObservationTags.WINE_ID, wineId);
        try {
            wineFeignClient.deleteWine(wineId);
        } catch (FeignException.NotFound e) {
            log.warn("Wine ID {} not found, skipping deletion", wineId);
            throw e;
        }
    }

    @Observed(name = ObservationNames.WINE_UPDATE_IMAGE)
    public void updateImages(Long id, MultipartFile imageA, MultipartFile imageB) {
        spanTagger.tag(ObservationTags.WINE_ID, id);
        wineFeignClient.updateWineImages(id, imageA, imageB);
    }
}