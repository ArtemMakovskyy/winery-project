package com.winestoreapp.wine.impl;

import com.winestoreapp.common.exception.EntityNotFoundException;
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
import io.micrometer.tracing.Tracer;

@Service
@Slf4j
public class WineServiceImpl implements WineService {

    private static final String IMAGE_API_PATH = "api/images/wine/";

    private final WineRepository wineRepository;
    private final WineMapper wineMapper;
    private final ImageStorageService imageStorageService;
    private final Tracer tracer;
    private final MeterRegistry registry;

    public WineServiceImpl(
            WineRepository wineRepository, WineMapper wineMapper,
            ImageStorageService imageStorageService, Tracer tracer,
            MeterRegistry registry
    ) {
        this.wineRepository = wineRepository;
        this.wineMapper = wineMapper;
        this.imageStorageService = imageStorageService;
        this.tracer = tracer;
        this.registry = registry;

        registry.gauge("wine.total.count", Tags.of("type", "database"), wineRepository, WineRepository::count);
    }

    @Override
    @Observed(name = "wine.service", contextualName = "add-wine")
    public WineDto add(WineCreateRequestDto createDto) {
        log.info("Adding new wine: {}", createDto.getName());
        Wine wine = wineMapper.toEntity(createDto);
        Wine savedWine = wineRepository.save(wine);
        addTracingTag("wine.id", savedWine.getId());
        return wineMapper.toDto(savedWine);
    }

    @Override
    @Transactional
    @Observed(name = "wine.service", contextualName = "update-wine-image")
    public WineDto updateImage(Long id, MultipartFile imageA, MultipartFile imageB) {
        log.info("Updating images for wine ID: {}", id);

        Wine wine = findWineById(id);
        addTracingTag("wine.id", id);

        imageStorageService.deleteImage(wine.getPictureLink());
        imageStorageService.deleteImage(wine.getPictureLink2());

        String nameA = imageStorageService.saveImage(imageA.getOriginalFilename(), "a", imageA);
        String nameB = imageStorageService.saveImage(imageB.getOriginalFilename(), "b", imageB);

        wine.setPictureLink(IMAGE_API_PATH + nameA);
        wine.setPictureLink2(IMAGE_API_PATH + nameB);

        return wineMapper.toDto(wineRepository.save(wine));
    }

    @Override
    @Observed(name = "wine.service", contextualName = "exists-wine")
    public boolean existsById(Long id) {
        addTracingTag("wine.id", id);
        return wineRepository.existsById(id);
    }

    @Override
    @Transactional
    @Observed(name = "wine.service", contextualName = "update-wine-rating")
    public void updateAverageRatingScore(Long id, Double score) {
        log.debug("Updating rating for wine ID {}: {}", id, score);

        Wine wine = findWineById(id);
        wine.setAverageRatingScore(BigDecimal.valueOf(score));
        wineRepository.save(wine);

        registry.gauge("wine.average.rating", Tags.of("wine.id", String.valueOf(id)), wine.getAverageRatingScore().doubleValue());

        addTracingTag("wine.id", id);
        addTracingTag("wine.score", score);
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "wine.service", contextualName = "find-all-wines")
    public List<WineDto> findAll(Pageable pageable) {
        return wineRepository.findAll(pageable).stream()
                .map(wineMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "wine.service", contextualName = "find-wine-by-id")
    public WineDto findById(Long id) {
        Wine wine = findWineById(id);
        addTracingTag("wine.id", id);
        return wineMapper.toDto(wine);
    }

    @Override
    @Transactional
    @Observed(name = "wine.service", contextualName = "delete-wine")
    public void deleteById(Long id) {
        log.info("Deleting wine with id={}", id);
        Wine wine = findWineById(id);
        wineRepository.delete(wine);
        addTracingTag("wine.id", id);
    }

    private Wine findWineById(Long id) {
        return wineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find wine by id: " + id));
    }

    private void addTracingTag(String key, Object value) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag(key, String.valueOf(value));
        }
    }
}