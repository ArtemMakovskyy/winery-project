package com.winestoreapp.wine.impl;

import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.observability.ObservationContextualNames;
import com.winestoreapp.common.observability.ObservationMetrics;
import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.common.observability.ObservationTags;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.wine.api.WineService;
import com.winestoreapp.wine.api.dto.WineCreateRequestDto;
import com.winestoreapp.wine.api.dto.WineDto;
import com.winestoreapp.wine.mapper.WineMapper;
import com.winestoreapp.wine.model.Wine;
import com.winestoreapp.wine.repository.WineRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.observation.annotation.Observed;

@Service
@Slf4j
public class WineServiceImpl implements WineService {

    private static final String IMAGE_API_PATH = "api/images/wine/";

    private final WineRepository wineRepository;
    private final WineMapper wineMapper;
    private final ImageStorageService imageStorageService;
    private final SpanTagger spanTagger;
    private final MeterRegistry registry;

    public WineServiceImpl(
            WineRepository wineRepository,
            WineMapper wineMapper,
            ImageStorageService imageStorageService,
            SpanTagger spanTagger,
            MeterRegistry registry
    ) {
        this.wineRepository = wineRepository;
        this.wineMapper = wineMapper;
        this.imageStorageService = imageStorageService;
        this.spanTagger = spanTagger;
        this.registry = registry;

        registry.gauge(
                ObservationMetrics.WINE_TOTAL_COUNT,
                Tags.of(ObservationTags.TYPE, "database"),
                wineRepository,
                WineRepository::count);
    }

    @Override
    @Observed(
            name = ObservationNames.WINE_SERVICE,
            contextualName = ObservationContextualNames.CREATE)
    public WineDto add(WineCreateRequestDto createDto) {
        log.info("Adding new wine: {}", createDto.getName());
        Wine wine = wineMapper.toEntity(createDto);
        Wine savedWine = wineRepository.save(wine);

        spanTagger.tag(
                ObservationTags.WINE_ID,
                savedWine.getId());

        return wineMapper.toDto(savedWine);
    }

    @Override
    @Transactional
    @Observed(name = ObservationNames.WINE_SERVICE,
            contextualName = ObservationContextualNames.UPDATE_IMAGE)
    public WineDto updateImage(Long id, MultipartFile imageA, MultipartFile imageB) {
        log.info("Updating images for wine ID: {}", id);

        Wine wine = findWineById(id);

        imageStorageService.deleteImage(wine.getPictureLink());
        imageStorageService.deleteImage(wine.getPictureLink2());

        String nameA = imageStorageService.saveImage(imageA.getOriginalFilename(), "a", imageA);
        String nameB = imageStorageService.saveImage(imageB.getOriginalFilename(), "b", imageB);

        wine.setPictureLink(IMAGE_API_PATH + nameA);
        wine.setPictureLink2(IMAGE_API_PATH + nameB);

        return wineMapper.toDto(wineRepository.save(wine));
    }

    @Override
    @Observed(name = ObservationNames.WINE_SERVICE,
            contextualName = ObservationContextualNames.EXISTS)
    public boolean existsById(Long id) {

        spanTagger.tag(ObservationTags.WINE_ID, id);

        return wineRepository.existsById(id);
    }

    @Override
    @Transactional
    @Observed(name = ObservationNames.WINE_SERVICE,
            contextualName = ObservationContextualNames.UPDATE_RATING)
    public void updateAverageRatingScore(Long id, Double score) {
        log.debug("Updating rating for wine ID {}: {}", id, score);

        Wine wine = findWineById(id);
        wine.setAverageRatingScore(BigDecimal.valueOf(score));
        wineRepository.save(wine);

        registry.gauge(
                ObservationMetrics.WINE_AVERAGE_RATING,
                Tags.of(
                        ObservationTags.WINE_ID,
                        String.valueOf(id)
                ),
                wine.getAverageRatingScore().doubleValue()
        );

        spanTagger.tag(ObservationTags.WINE_SCORE, score);
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = ObservationNames.WINE_SERVICE,
            contextualName = ObservationContextualNames.FIND_ALL)
    public List<WineDto> findAll(Pageable pageable) {
        return wineRepository.findAll(pageable).stream()
                .map(wineMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = ObservationNames.WINE_SERVICE,
            contextualName = ObservationContextualNames.FIND_BY_ID)
    public WineDto findById(Long id) {
        Wine wine = findWineById(id);
        return wineMapper.toDto(wine);
    }

    @Override
    @Transactional
    @Observed(name = ObservationNames.WINE_SERVICE,
            contextualName = ObservationContextualNames.DELETE_BY_ID)
    public void deleteById(Long id) {
        log.info("Deleting wine with id={}", id);
        Wine wine = findWineById(id);
        wineRepository.delete(wine);
    }

    private Wine findWineById(Long id) {

        spanTagger.tag(ObservationTags.WINE_ID, id);

        try {

            Wine wine = wineRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Can't find wine by id: " + id));

            spanTagger.tag(ObservationTags.STATUS, "success");

            return wine;
        } catch (EntityNotFoundException ex) {

            log.warn("Wine not found. id={}", id);
            spanTagger.tag(ObservationTags.STATUS, "error");
            spanTagger.tag(ObservationTags.ERROR_MESSAGE, ex.getMessage());

            throw ex;
        }
    }

}
