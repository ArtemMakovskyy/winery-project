package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WineUiControllerTest {

    @Mock
    private WineService wineService;

    @Mock
    private Tracer tracer;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Span span;

    @InjectMocks
    private WineUiController wineUiController;

    @BeforeEach
    void setup() {
        lenient().when(tracer.currentSpan()).thenReturn(span);
        lenient().when(span.tag(anyString(), anyString())).thenReturn(span);
    }

    @Test
    void list_ShouldPopulateModelWithWines() {
        List<WineDto> wines = List.of(new WineDto());
        when(wineService.getAll()).thenReturn(wines);

        String view = wineUiController.list(model);

        assertThat(view).isEqualTo("wine/wines");
        verify(model).addAttribute("wines", wines);
        verify(model).addAttribute(eq("wine"), any(WineCreateRequestDto.class));
    }

    @Test
    void create_WhenValidationFails_ShouldReturnListViewWithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(wineService.getAll()).thenReturn(List.of());

        String view = wineUiController.create(new WineCreateRequestDto(), bindingResult, model);

        assertThat(view).isEqualTo("wine/wines");
        verify(span).tag("validation.status", "failed");
        verify(model).addAttribute(eq("wines"), anyList());
        verify(wineService, never()).create(any());
    }

    @Test
    void create_OnSuccess_ShouldTagAndRedirect() {
        WineCreateRequestDto dto = new WineCreateRequestDto();
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = wineUiController.create(dto, bindingResult, model);

        assertThat(view).isEqualTo("redirect:/ui/wines");
        verify(wineService).create(dto);
        verify(span).tag("status", "success");
    }

    @Test
    void create_OnServiceException_ShouldReturnListViewWithError() {
        RuntimeException ex = new RuntimeException("DB Error");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(wineService.create(any())).thenThrow(ex);
        when(wineService.getAll()).thenReturn(List.of());

        String view = wineUiController.create(new WineCreateRequestDto(), bindingResult, model);

        assertThat(view).isEqualTo("wine/wines");
        verify(model).addAttribute("error", "DB Error");
        verify(span).error(ex);
    }

    @Test
    void delete_OnSuccess_ShouldTagAndRedirect() {
        Long id = 10L;

        String view = wineUiController.delete(id, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/ui/wines");
        verify(wineService).delete(id);
        verify(span).tag("wine.id", "10");
        verify(redirectAttributes).addFlashAttribute("message", "Wine successfully deleted");
    }

    @Test
    void delete_OnException_ShouldRedirectWithError() {
        Long id = 10L;
        doThrow(new RuntimeException("Fail")).when(wineService).delete(id);

        wineUiController.delete(id, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "Fail");
    }

    @Test
    void handleImageUpload_OnSuccess_ShouldRedirect() {
        MultipartFile fileA = mock(MultipartFile.class);
        MultipartFile fileB = mock(MultipartFile.class);

        String view = wineUiController.handleImageUpload(1L, fileA, fileB, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/ui/wines");
        verify(wineService).updateImages(1L, fileA, fileB);
        verify(redirectAttributes).addFlashAttribute(eq("message"), anyString());
    }
}