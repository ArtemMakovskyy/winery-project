package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import feign.FeignException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WineServiceTest {

    @Mock
    private WineFeignClient wineFeignClient;

    @Mock
    private FeignErrorParser errorParser;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    private WineService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(tracer.currentSpan()).thenReturn(span);
        service = new WineService(wineFeignClient, errorParser, tracer);
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
        verify(span).tag("status", "success");
        verify(span).tag("wine.id", "10");
    }

    @Test
    void create_feignError_shouldThrowRuntimeException() {
        WineCreateRequestDto dto = new WineCreateRequestDto();
        dto.setName("Test Wine");

        FeignException ex = mock(FeignException.class);
        when(wineFeignClient.createWine(dto)).thenThrow(ex);
        when(errorParser.extractMessage(ex)).thenReturn("Create failed");

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Create failed");

        verify(errorParser).extractMessage(ex);
        verify(span).tag("status", "error");
        verify(span).error(ex);
    }

    @Test
    void getAll_success() {
        when(wineFeignClient.getAll()).thenReturn(List.of(new WineDto(), new WineDto()));

        List<WineDto> result = service.getAll();

        assertThat(result).hasSize(2);
        verify(wineFeignClient).getAll();
    }

    @Test
    void delete_success() {
        doNothing().when(wineFeignClient).deleteWine(5L);

        service.delete(5L);

        verify(wineFeignClient).deleteWine(5L);
        verify(span).tag("status", "success");
    }

    @Test
    void delete_notFound_shouldNotThrow() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(404);
        doThrow(ex).when(wineFeignClient).deleteWine(5L);

        assertThatCode(() -> service.delete(5L)).doesNotThrowAnyException();
        verify(span).tag("status", "not_found");
    }

    @Test
    void delete_otherFeignError_shouldThrowRuntimeException() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(500);
        doThrow(ex).when(wineFeignClient).deleteWine(5L);
        when(errorParser.extractMessage(ex)).thenReturn("Delete failed");

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Delete failed");

        verify(errorParser).extractMessage(ex);
        verify(span).tag("status", "error");
        verify(span).error(ex);
    }

    @Test
    void updateImages_success() {
        MultipartFile fileA = mock(MultipartFile.class);
        MultipartFile fileB = mock(MultipartFile.class);

        when(wineFeignClient.updateWineImages(1L, fileA, fileB)).thenReturn(new WineDto());

        service.updateImages(1L, fileA, fileB);

        verify(wineFeignClient).updateWineImages(1L, fileA, fileB);
    }

    @Test
    void updateImages_feignError_shouldThrowRuntimeException() {
        MultipartFile fileA = mock(MultipartFile.class);
        MultipartFile fileB = mock(MultipartFile.class);
        FeignException ex = mock(FeignException.class);

        doThrow(ex).when(wineFeignClient).updateWineImages(1L, fileA, fileB);
        when(errorParser.extractMessage(ex)).thenReturn("Upload failed");

        assertThatThrownBy(() -> service.updateImages(1L, fileA, fileB))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Upload failed");
    }
}
