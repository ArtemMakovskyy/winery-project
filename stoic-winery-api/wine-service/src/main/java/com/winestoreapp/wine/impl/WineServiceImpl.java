package com.winestoreapp.wine.impl;

import com.winestoreapp.common.exception.EntityNotFoundException;
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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.observation.annotation.Observed;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class WineServiceImpl implements WineService {

    private final WineRepository wineRepository;
    private final WineMapper wineMapper;
    private final ImageStorageService imageStorageService;
    private final SpanTagger spanTagger;
    private final MeterRegistry registry;

    @Value("${image.link.path}")
    private String imageLinkPath;

    public WineServiceImpl(WineRepository wineRepository, WineMapper wineMapper,
                           ImageStorageService imageStorageService, SpanTagger spanTagger,
                           MeterRegistry registry) {
        this.wineRepository = wineRepository;
        this.wineMapper = wineMapper;
        this.imageStorageService = imageStorageService;
        this.spanTagger = spanTagger;
        this.registry = registry;

        registry.gauge(ObservationMetrics.WINE_TOTAL_COUNT,
                Tags.of(ObservationTags.TYPE, "database"),
                wineRepository, WineRepository::count);
    }

    @Override
    @Observed(name = ObservationNames.WINE_CREATE)
    @CacheEvict(value = {"wines", "winesList"}, allEntries = true)
    public WineDto add(WineCreateRequestDto createDto) {
        Wine wine = wineMapper.toEntity(createDto);
        Wine savedWine = wineRepository.save(wine);
        spanTagger.tag(ObservationTags.WINE_ID, savedWine.getId());
        return wineMapper.toDto(savedWine);
    }

    @Override
    @Transactional
    @Observed(name = ObservationNames.WINE_UPDATE_IMAGE)
    @CacheEvict(value = {"wines", "winesList"}, allEntries = true)
    public WineDto updateImage(Long id, MultipartFile imageA, MultipartFile imageB) {
        Wine wine = findWineById(id);
        imageStorageService.deleteImage(wine.getPictureLink());
        imageStorageService.deleteImage(wine.getPictureLink2());
        String nameA = imageStorageService.saveImage(imageA.getOriginalFilename(), "a", imageA);
        String nameB = imageStorageService.saveImage(imageB.getOriginalFilename(), "b", imageB);
        wine.setPictureLink(imageLinkPath + nameA);
        wine.setPictureLink2(imageLinkPath + nameB);
        return wineMapper.toDto(wineRepository.save(wine));
    }

    @Override
    @Observed(name = ObservationNames.WINE_EXISTS)
    public boolean existsById(Long id) {
        spanTagger.tag(ObservationTags.WINE_ID, id);
        return wineRepository.existsById(id);
    }

    @Override
    @Transactional
    @Observed(name = ObservationNames.WINE_UPDATE_RATING)
    @CacheEvict(value = {"wines", "winesList"}, allEntries = true)
    public void updateAverageRatingScore(Long id, Double score) {
        Wine wine = findWineById(id);
        wine.setAverageRatingScore(BigDecimal.valueOf(score));
        wineRepository.save(wine);
        registry.gauge(ObservationMetrics.WINE_AVERAGE_RATING,
                Tags.of(ObservationTags.WINE_ID, String.valueOf(id)),
                wine.getAverageRatingScore().doubleValue());
        spanTagger.tag(ObservationTags.WINE_SCORE, score);
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = ObservationNames.WINE_FIND_ALL)
    @Cacheable(value = "winesList",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()",
            cacheManager = "mediumTtlCacheManager")
    public List<WineDto> findAll(Pageable pageable) {
        return wineRepository.findAll(pageable).stream()
                .map(wineMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = ObservationNames.WINE_FIND)
    @Cacheable(value = "wines", key = "#id", cacheManager = "mediumTtlCacheManager")
    public WineDto findById(Long id) {
        Wine wine = findWineById(id);
        return wineMapper.toDto(wine);
    }

    @Override
    @Transactional
    @Observed(name = ObservationNames.WINE_DELETE)
    @CacheEvict(value = {"wines", "winesList"}, allEntries = true)
    public void deleteById(Long id) {
        Wine wine = findWineById(id);
        wineRepository.delete(wine);
    }

    private Wine findWineById(Long id) {
        spanTagger.tag(ObservationTags.WINE_ID, id);
        return wineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find wine by id: " + id));
    }
}