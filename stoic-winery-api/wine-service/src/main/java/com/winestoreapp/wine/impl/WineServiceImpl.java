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

    @Override
    public WineDto add(WineCreateRequestDto createDto) {
        final Wine wine = wineMapper.toEntity(createDto);
        final Wine savedWine = wineRepository.save(wine);
        return wineMapper.toDto(savedWine);
    }

    @Override
    @Transactional
    public WineDto updateImage(Long id, MultipartFile imageA, MultipartFile imageB) {
        Wine wine = findWineById(id);
        log.info("Updating images for wine ID: {}", id);

        imageStorageService.deleteImage(wine.getPictureLink());
        imageStorageService.deleteImage(wine.getPictureLink2());

        String nameA = imageStorageService.saveImage(imageA.getOriginalFilename(), "a", imageA);
        String nameB = imageStorageService.saveImage(imageB.getOriginalFilename(), "b", imageB);

        wine.setPictureLink(IMAGE_API_PATH + nameA);
        wine.setPictureLink2(IMAGE_API_PATH + nameB);

        return wineMapper.toDto(wineRepository.save(wine));
    }

    @Override
    public boolean existsById(Long id) {
        return wineRepository.existsById(id);
    }

    @Override
    @Transactional
    public void updateAverageRatingScore(Long id, Double score) {
        Wine wine = findWineById(id);
        wine.setAverageRatingScore(BigDecimal.valueOf(score));
        wineRepository.save(wine);
    }

    @Override
    public List<WineDto> findAll(Pageable pageable) {
        return wineRepository.findAll(pageable).stream()
                .map(wineMapper::toDto)
                .toList();
    }

    @Override
    public WineDto findById(Long id) {
        return wineMapper.toDto(findWineById(id));
    }

    @Override
    public boolean isDeleteById(Long id) {
        if (!wineRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't find wine by id: " + id);
        }
        wineRepository.deleteById(id);
        return true;
    }

    private Wine findWineById(Long id) {
        return wineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find wine by id: " + id));
    }
}
