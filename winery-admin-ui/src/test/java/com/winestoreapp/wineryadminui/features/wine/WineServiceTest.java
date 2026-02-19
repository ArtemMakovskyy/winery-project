package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class WineServiceTest {

    @Mock
    private WineFeignClient wineFeignClient;

    @Mock
    private SpanTagger spanTagger;

    private WineService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new WineService(wineFeignClient, spanTagger);
    }

    @Test
    void create_success() {
        WineCreateRequestDto dto = new WineCreateRequestDto();
        dto.setName("Test Wine");
        dto.setVendorCode("VC-123");

        WineDto created = new WineDto();
        created.setId(10L);

        when(wineFeignClient.createWine(dto)).thenReturn(created);

        WineDto result = service.create(dto);

        assertThat(result.getId()).isEqualTo(10L);

        verify(spanTagger).tag(ObservationTags.VENDOR_CODE, "VC-123");
        verify(spanTagger).tag(ObservationTags.WINE_NAME, "Test Wine");

        verify(wineFeignClient).createWine(dto);
    }


    @Test
    void getAll_success() {
        when(wineFeignClient.getAll())
                .thenReturn(List.of(new WineDto(), new WineDto()));

        List<WineDto> result = service.getAll();

        assertThat(result).hasSize(2);
        verify(wineFeignClient).getAll();
    }

    @Test
    void delete_success() {
        service.delete(5L);

        verify(spanTagger).tag("wine.id", 5L);
        verify(wineFeignClient).deleteWine(5L);
    }

    @Test
    void delete_notFound_shouldThrow() {
        FeignException.NotFound ex = mock(FeignException.NotFound.class);
        doThrow(ex).when(wineFeignClient).deleteWine(5L);

        assertThatCode(() -> service.delete(5L))
                .isInstanceOf(FeignException.NotFound.class);

        verify(spanTagger).tag("wine.id", 5L);
        verify(wineFeignClient).deleteWine(5L);
    }

    @Test
    void updateImages_success() {
        MultipartFile fileA = mock(MultipartFile.class);
        MultipartFile fileB = mock(MultipartFile.class);

        when(wineFeignClient.updateWineImages(1L, fileA, fileB)).thenReturn(null);

        service.updateImages(1L, fileA, fileB);

        verify(spanTagger).tag("wine.id", 1L);
        verify(wineFeignClient).updateWineImages(1L, fileA, fileB);
    }
}
