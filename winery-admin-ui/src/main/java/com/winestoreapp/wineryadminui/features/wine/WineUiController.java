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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui/wines")
@RequiredArgsConstructor
@Slf4j
public class WineUiController {

    private final WineService wineService;

    @PostMapping
    public String create(@Valid @ModelAttribute("wine") WineCreateRequestDto createDto,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            log.warn("Wine creation validation failed: {} errors found", bindingResult.getErrorCount());
            model.addAttribute("wines", wineService.getAll());
            return "wine/wines";
        }

        try {
            wineService.create(createDto);
            return "redirect:/ui/wines";
        } catch (RuntimeException e) {
            log.error("Business error during wine creation: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("wines", wineService.getAll());
            return "wine/wines";
        }
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long wineId, RedirectAttributes redirectAttributes) {
        try {
            wineService.delete(wineId);
            redirectAttributes.addFlashAttribute("message", "Wine successfully deleted");
        } catch (Exception e) {
            log.error("UI Error: Could not delete wine ID {}", wineId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/wines";
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("wines", wineService.getAll());
        model.addAttribute("wine", new WineCreateRequestDto());
        return "wine/wines";
    }
}