package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.observability.ObservationContextualNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui/wines")
@RequiredArgsConstructor
@Slf4j
public class WineUiController {

    private final WineService wineService;
    private final SpanTagger spanTagger;

    @GetMapping
    @Observed(name = ObservationNames.WINE_CONTROLLER,
            contextualName = ObservationContextualNames.WINE_FORM
    )
    public String list(Model model) {
        model.addAttribute("wines", wineService.getAll());
        model.addAttribute("wine", new WineCreateRequestDto());
        return "wine/wines";
    }

    @PostMapping
    @Observed(name = ObservationNames.WINE_CONTROLLER,
            contextualName = ObservationContextualNames.CREATE,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public String create(
            @Valid @ModelAttribute("wine") WineCreateRequestDto createDto,
            BindingResult bindingResult, Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("wines", wineService.getAll());
            return "wine/wines";
        }

        spanTagger.tag(ObservationTags.WINE_NAME, createDto.getName());
        wineService.create(createDto);
        return "redirect:/ui/wines";
    }

    @PostMapping("/delete")
    @Observed(name = ObservationNames.WINE_CONTROLLER,
            contextualName = ObservationContextualNames.DELETE_BY_ID,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public String delete(@RequestParam Long wineId, RedirectAttributes redirectAttributes) {
        spanTagger.tag(ObservationTags.WINE_ID, wineId);
        try {
            wineService.delete(wineId);
            redirectAttributes.addFlashAttribute("message", "Wine deleted successfully");
        } catch (FeignException.NotFound e) {
            redirectAttributes.addFlashAttribute("error", "Wine with ID " + wineId + " not found");
        }
        return "redirect:/ui/wines";
    }

    @PostMapping("/upload-images-proxy")
    @Observed(name = ObservationNames.WINE_CONTROLLER,
            contextualName = ObservationContextualNames.UPDATE_IMAGE,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public String handleImageUpload(
            @RequestParam("wineId") Long id,
            @RequestParam("imageA") MultipartFile imageA,
            @RequestParam("imageB") MultipartFile imageB,
            RedirectAttributes redirectAttributes
    ) {
        spanTagger.tag(ObservationTags.WINE_ID, id);
        wineService.updateImages(id, imageA, imageB);
        redirectAttributes.addFlashAttribute("message", "Images updated successfully!");
        return "redirect:/ui/wines";
    }
}