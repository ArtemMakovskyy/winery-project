package com.winestoreapp.wine.impl;

import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.wine.api.WineService;
import com.winestoreapp.wine.api.dto.WineCreateRequestDto;
import com.winestoreapp.wine.api.dto.WineDto;
import com.winestoreapp.wine.mapper.WineMapper;
import com.winestoreapp.wine.model.Wine;
import com.winestoreapp.wine.repository.WineRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class WineServiceImpl implements WineService {

    private static final String IMAGE_API_PATH = "api/images/wine/";

    private final WineRepository wineRepository;
    private final WineMapper wineMapper;
    private final ImageStorageService imageStorageService;
    private final Tracer tracer;
    private final MeterRegistry registry;

    @Override
    @Observed(name = "wine.service", contextualName = "add-wine")
    @Counted(value = "wine.add.count", description = "Number of times add wine is called")
    @Timed(value = "wine.add.time", description = "Time taken to add a wine")
    public WineDto add(WineCreateRequestDto createDto) {
        log.info("Adding new wine: {}", createDto.getName());
        final Wine wine = wineMapper.toEntity(createDto);
        try {
            final Wine savedWine = wineRepository.save(wine);
            registry.gauge("wine.total.count", Tags.of("operation", "add"), wineRepository.count());
            return wineMapper.toDto(savedWine);
        } catch (Exception e) {
            registry.counter("wine.errors", "method", "add", "type", e.getClass().getSimpleName()).increment();
            throw e;
        }
    }

    @Override
    @Transactional
    @Observed(name = "wine.service", contextualName = "update-wine-image")
    @Counted(value = "wine.update.image.count", description = "Number of times wine images updated")
    @Timed(value = "wine.update.image.time", description = "Time taken to update wine images")
    public WineDto updateImage(Long id, MultipartFile imageA, MultipartFile imageB) {
        log.info("Updating images for wine ID: {}", id);
        Wine wine = null;
        try {
            wine = findWineById(id);
            if (tracer.currentSpan() != null) {
                tracer.currentSpan().tag("wine.id", String.valueOf(id));
            }

            imageStorageService.deleteImage(wine.getPictureLink());
            imageStorageService.deleteImage(wine.getPictureLink2());

            String nameA = imageStorageService.saveImage(imageA.getOriginalFilename(), "a", imageA);
            String nameB = imageStorageService.saveImage(imageB.getOriginalFilename(), "b", imageB);

            wine.setPictureLink(IMAGE_API_PATH + nameA);
            wine.setPictureLink2(IMAGE_API_PATH + nameB);

            Wine savedWine = wineRepository.save(wine);
            return wineMapper.toDto(savedWine);

        } catch (Exception e) {
            registry.counter("wine.errors", "method", "updateImage", "type", e.getClass().getSimpleName()).increment();
            throw e;
        }
    }

    @Override
    @Counted(value = "wine.exists.count", description = "Number of times existsById called")
    @Timed(value = "wine.exists.time", description = "Time taken for existsById check")
    public boolean existsById(Long id) {
        return wineRepository.existsById(id);
    }

    @Override
    @Transactional
    @Observed(name = "wine.service", contextualName = "update-wine-rating")
    @Counted(value = "wine.update.rating.count", description = "Number of times wine rating updated")
    @Timed(value = "wine.update.rating.time", description = "Time taken to update rating")
    public void updateAverageRatingScore(Long id, Double score) {
        log.debug("Updating rating for wine ID {}: {}", id, score);
        try {
            Wine wine = findWineById(id);
            wine.setAverageRatingScore(BigDecimal.valueOf(score));
            wineRepository.save(wine);

            registry.gauge("wine.average.rating", Tags.of("wine.id", String.valueOf(id)), wine.getAverageRatingScore().doubleValue());
            if (tracer.currentSpan() != null) {
                tracer.currentSpan().tag("wine.id", String.valueOf(id));
                tracer.currentSpan().tag("wine.score", String.valueOf(score));
            }
        } catch (Exception e) {
            registry.counter("wine.errors", "method", "updateAverageRatingScore", "type", e.getClass().getSimpleName()).increment();
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "wine.service", contextualName = "find-all-wines")
    @Counted(value = "wine.find.all.count", description = "Number of times findAll called")
    @Timed(value = "wine.find.all.time", description = "Time taken to fetch all wines")
    public List<WineDto> findAll(Pageable pageable) {
        List<WineDto> wines = wineRepository.findAll(pageable).stream()
                .map(wineMapper::toDto)
                .toList();
        registry.gauge("wine.total.count", Tags.of("operation", "findAll"), wineRepository.count());
        return wines;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "wine.service", contextualName = "find-wine-by-id")
    @Counted(value = "wine.find.by.id.count", description = "Number of times findById called")
    @Timed(value = "wine.find.by.id.time", description = "Time taken to fetch wine by id")
    public WineDto findById(Long id) {
        try {
            Wine wine = findWineById(id);
            if (tracer.currentSpan() != null) {
                tracer.currentSpan().tag("wine.id", String.valueOf(id));
            }
            return wineMapper.toDto(wine);
        } catch (EntityNotFoundException e) {
            registry.counter("wine.errors", "method", "findById", "type", "not_found").increment();
            throw e;
        }
    }

    @Override
    @Transactional
    @Observed(name = "wine.service", contextualName = "delete-wine")
    @Counted(value = "wine.delete.count", description = "Number of times wine deleted")
    @Timed(value = "wine.delete.time", description = "Time taken to delete wine")
    public boolean isDeleteById(Long id) {
        log.info("Deleting wine with ID: {}", id);
        if (!wineRepository.existsById(id)) {
            registry.counter("wine.errors", "method", "delete", "type", "not_found").increment();
            throw new EntityNotFoundException("Can't find wine by id: " + id);
        }
        wineRepository.deleteById(id);
        registry.gauge("wine.total.count", Tags.of("operation", "delete"), wineRepository.count());
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("wine.id", String.valueOf(id));
        }
        return true;
    }

    private Wine findWineById(Long id) {
        return wineRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Wine not found with ID: {}", id);
                    return new EntityNotFoundException("Can't find wine by id: " + id);
                });
    }
}
