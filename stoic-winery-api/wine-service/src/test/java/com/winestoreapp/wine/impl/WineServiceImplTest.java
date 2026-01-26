package com.winestoreapp.wine.impl;

import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.wine.api.dto.WineCreateRequestDto;
import com.winestoreapp.wine.api.dto.WineDto;
import com.winestoreapp.wine.mapper.WineMapper;
import com.winestoreapp.wine.model.Wine;
import com.winestoreapp.wine.repository.WineRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WineServiceImplTest {

    @Mock
    private Tracer tracer;

    @Mock
    private MeterRegistry registry;

    @Mock
    private WineRepository wineRepository;

    @Mock
    private WineMapper wineMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private WineServiceImpl wineService;

    private Wine wine;
    private WineDto wineDto;

    @BeforeEach
    void setUp() {
        wine = new Wine();
        wine.setId(1L);
        wine.setName("Test Wine");
        wine.setPrice(BigDecimal.TEN);

        wineDto = new WineDto();
        wineDto.setId(1L);
        wineDto.setName("Test Wine");
        wineDto.setPrice(BigDecimal.TEN);

        Counter mockCounter = mock(Counter.class);
        lenient().when(registry.counter(anyString(), any(String[].class))).thenReturn(mockCounter);
    }

    @Test
    void add_ValidDto_ShouldReturnWineDto() {
        WineCreateRequestDto requestDto = new WineCreateRequestDto();
        requestDto.setName("Test Wine");

        when(wineMapper.toEntity(requestDto)).thenReturn(wine);
        when(wineRepository.save(wine)).thenReturn(wine);
        when(wineMapper.toDto(wine)).thenReturn(wineDto);

        WineDto result = wineService.add(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Wine");

        verify(wineRepository).save(wine);
    }

    @Test
    void findById_ExistingId_ShouldReturnWineDto() {
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));
        when(wineMapper.toDto(wine)).thenReturn(wineDto);

        WineDto result = wineService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_NotExistingId_ShouldThrowException() {
        when(wineRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wineService.findById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_ShouldReturnList() {
        when(wineRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(wine)));
        when(wineMapper.toDto(wine)).thenReturn(wineDto);

        List<WineDto> result = wineService.findAll(PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    void existsById_ShouldReturnTrue() {
        when(wineRepository.existsById(1L)).thenReturn(true);

        assertThat(wineService.existsById(1L)).isTrue();
    }

    @Test
    void deleteById_ExistingId_ShouldDeleteWine() {
        when(wineRepository.existsById(1L)).thenReturn(true);

        assertThatCode(() -> wineService.deleteById(1L))
                .doesNotThrowAnyException();

        verify(wineRepository).deleteById(1L);
    }

    @Test
    void deleteById_NotExistingId_ShouldThrowException() {
        when(wineRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> wineService.deleteById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Wine not found");
    }

    @Test
    void updateAverageRatingScore_ShouldUpdateScore() {
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));

        wineService.updateAverageRatingScore(1L, 4.5);

        assertThat(wine.getAverageRatingScore())
                .isEqualByComparingTo(BigDecimal.valueOf(4.5));
    }

    @Test
    void updateImage_ShouldReplaceImages() {
        MultipartFile imageA = mock(MultipartFile.class);
        MultipartFile imageB = mock(MultipartFile.class);

        wine.setPictureLink("oldA.jpg");
        wine.setPictureLink2("oldB.jpg");

        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));
        when(imageStorageService.saveImage(any(), eq("a"), eq(imageA)))
                .thenReturn("newA.jpg");
        when(imageStorageService.saveImage(any(), eq("b"), eq(imageB)))
                .thenReturn("newB.jpg");
        when(wineMapper.toDto(any())).thenReturn(wineDto);
        when(wineRepository.save(any())).thenReturn(wine);

        WineDto result = wineService.updateImage(1L, imageA, imageB);

        assertThat(result).isNotNull();
        verify(imageStorageService).deleteImage("oldA.jpg");
        verify(imageStorageService).deleteImage("oldB.jpg");
    }
}
