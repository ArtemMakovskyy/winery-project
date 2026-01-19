package com.winestoreapp.wine.api;

import com.winestoreapp.wine.api.dto.WineCreateRequestDto;
import com.winestoreapp.wine.api.dto.WineDto;
import java.net.MalformedURLException;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface WineService {
    WineDto add(WineCreateRequestDto createDto);

    List<WineDto> findAll(Pageable pageable);

    WineDto findById(Long id);

    boolean isDeleteById(Long id);

    WineDto updateImage(Long id, MultipartFile imageA, MultipartFile imageB)
            throws MalformedURLException;

    boolean existsById(Long id);

    void updateAverageRatingScore(Long id, Double score);
}