package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WineUiControllerTest {

    @Mock
    private WineService wineService;

    @Mock
    private SpanTagger spanTagger;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private WineUiController wineUiController;

    @Test
    void list_ShouldPopulateModelWithWines() {
        List<WineDto> wines = List.of(new WineDto());
        when(wineService.getAll()).thenReturn(wines);

        String view = wineUiController.list(model);

        assertThat(view).isEqualTo("wine/wines");
        verify(model).addAttribute("wines", wines);
        verify(model).addAttribute(eq("wine"), refEq(new WineCreateRequestDto()));
    }


    @Test
    void create_WhenValidationFails_ShouldReturnListView() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(wineService.getAll()).thenReturn(List.of());

        String view = wineUiController.create(
                new WineCreateRequestDto(), bindingResult, model);

        assertThat(view).isEqualTo("wine/wines");
        verify(model).addAttribute("wines", List.of());
        verify(wineService, never()).create(any());
    }

    @Test
    void create_OnSuccess_ShouldTagAndRedirect() {
        WineCreateRequestDto dto = new WineCreateRequestDto();
        dto.setName("Test Wine");

        when(bindingResult.hasErrors()).thenReturn(false);

        String view = wineUiController.create(dto, bindingResult, model);

        assertThat(view).isEqualTo("redirect:/ui/wines");

        verify(spanTagger).tag("wine.name", "Test Wine");
        verify(wineService).create(dto);
    }

    @Test
    void delete_ShouldTagCallServiceAndRedirect() {
        Long id = 10L;
        doNothing().when(wineService).delete(id);

        String view = wineUiController.delete(id, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/ui/wines");
        verify(spanTagger).tag("wine.id", 10L);
        verify(wineService).delete(id);
        verify(redirectAttributes)
                .addFlashAttribute("message", "Wine deleted successfully");
    }

    @Test
    void handleImageUpload_ShouldCallServiceAndRedirect() {
        MultipartFile fileA = mock(MultipartFile.class);
        MultipartFile fileB = mock(MultipartFile.class);

        String view = wineUiController.handleImageUpload(
                1L, fileA, fileB, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/ui/wines");

        verify(spanTagger).tag("wine.id", 1L);
        verify(wineService).updateImages(1L, fileA, fileB);
        verify(redirectAttributes)
                .addFlashAttribute("message", "Images updated successfully!");
    }
}
