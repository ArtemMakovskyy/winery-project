package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String create(@ModelAttribute WineCreateRequestDto createDto) {
        wineService.create(createDto);
        return "redirect:/ui/wines";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long wineId, RedirectAttributes redirectAttributes) {
        try {
            wineService.delete(wineId);
            redirectAttributes.addFlashAttribute("message", "Wine successfully deleted");
        } catch (feign.FeignException.NotFound e) {
            redirectAttributes.addFlashAttribute("error", "Wine with ID " + wineId + " not found in the database");
        } catch (Exception e) {
            log.error("Unexpected UI error during deletion of wine ID {}", wineId, e);
            redirectAttributes.addFlashAttribute("error", "An error occurred during deletion");
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
