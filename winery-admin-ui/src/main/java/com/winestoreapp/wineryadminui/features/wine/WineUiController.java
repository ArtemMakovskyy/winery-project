package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Controller
@RequestMapping("/ui/wines")
@RequiredArgsConstructor
@Slf4j
public class WineUiController {

    private final WineService wineService;
    private final Tracer tracer;

    @GetMapping
    @Observed(name = "ui.wine.list")
    public String list(Model model) {
        model.addAttribute("wines", wineService.getAll());
        model.addAttribute("wine", new WineCreateRequestDto());
        return "wine/wines";
    }

    @PostMapping
    @Observed(name = "ui.wine.create_submit")
    public String create(@Valid @ModelAttribute("wine") WineCreateRequestDto createDto,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            log.warn("Wine creation validation failed: {} errors", bindingResult.getErrorCount());
            tagSpan("validation.status", "failed");
            model.addAttribute("wines", wineService.getAll());
            return "wine/wines";
        }

        tagSpan("wine.name", createDto.getName());

        try {
            wineService.create(createDto);
            tagSpan("status", "success");
            return "redirect:/ui/wines";
        } catch (RuntimeException e) {
            log.error("Wine creation failed: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("wines", wineService.getAll());

            tagSpan("status", "error");
            recordError(e);

            return "wine/wines";
        }
    }

    @PostMapping("/delete")
    @Observed(name = "ui.wine.delete_submit")
    public String delete(@RequestParam Long wineId, RedirectAttributes redirectAttributes) {
        tagSpan("wine.id", wineId);

        try {
            wineService.delete(wineId);
            redirectAttributes.addFlashAttribute("message", "Wine successfully deleted");
            tagSpan("status", "success");
        } catch (Exception e) {
            log.error("Failed to delete wine ID {}: {}", wineId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            tagSpan("status", "error");
            recordError(e);
        }

        return "redirect:/ui/wines";
    }

    @PostMapping("/upload-images-proxy")
    @Observed(name = "ui.wine.upload_images")
    public String handleImageUpload(
            @RequestParam("wineId") Long id,
            @RequestParam("imageA") MultipartFile imageA,
            @RequestParam("imageB") MultipartFile imageB,
            RedirectAttributes redirectAttributes) {

        tagSpan("wine.id", id);
        try {
            wineService.updateImages(id, imageA, imageB);
            redirectAttributes.addFlashAttribute("message", "Images updated successfully!");
            tagSpan("status", "success");
        } catch (RuntimeException e) {
            log.error("Upload failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            tagSpan("status", "error");
            recordError(e);
        }

        return "redirect:/ui/wines";
    }

    private void tagSpan(String key, Object value) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, String.valueOf(value));
        }
    }

    private void recordError(Throwable e) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.error(e);
        }
    }
}
