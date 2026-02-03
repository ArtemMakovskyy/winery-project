package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import jakarta.validation.Valid;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        var span = tracer.currentSpan();

        if (bindingResult.hasErrors()) {
            log.warn("Wine creation validation failed: {} errors", bindingResult.getErrorCount());
            if (span != null) { span.tag("validation.status", "failed"); }
            model.addAttribute("wines", wineService.getAll());
            return "wine/wines";
        }

        try {
            wineService.create(createDto);
            if (span != null) { span.tag("status", "success"); }
            return "redirect:/ui/wines";
        } catch (RuntimeException e) {
            log.error("Wine creation failed: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("wines", wineService.getAll());
            if (span != null) { span.tag("status", "error").error(e); }
            return "wine/wines";
        }
    }

    @PostMapping("/delete")
    @Observed(name = "ui.wine.delete_submit")
    public String delete(@RequestParam Long wineId, RedirectAttributes redirectAttributes) {
        var span = tracer.currentSpan();
        if (span != null) { span.tag("wine.id", String.valueOf(wineId)); }

        try {
            wineService.delete(wineId);
            redirectAttributes.addFlashAttribute("message", "Wine successfully deleted");
            if (span != null) { span.tag("status", "success"); }
        } catch (Exception e) {
            log.error("Failed to delete wine ID {}: {}", wineId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (span != null) { span.tag("status", "error").error(e); }
        }

        return "redirect:/ui/wines";
    }
}
