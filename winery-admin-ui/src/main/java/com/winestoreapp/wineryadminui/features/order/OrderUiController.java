package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderUiController {

    private final OrderService orderService;
    private final SpanTagger spanTagger;

    @GetMapping
    @Observed(name = ObservationNames.UI_ORDER_FORM)
    public String list(Model model) {
        model.addAttribute("orders", orderService.getAll());
        return "order/orders";
    }

    @PostMapping("/{id}/paid")
    @Observed(name = ObservationNames.ORDER_SET_PAID,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public String setPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        spanTagger.tag(ObservationTags.ORDER_ID, id);
        orderService.setPaidStatus(id);
        redirectAttributes.addFlashAttribute("message", "Order marked as paid");
        return "redirect:/ui/orders";
    }

    @PostMapping("/{id}/delete")
    @Observed(name = ObservationNames.ORDER_DELETE,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        spanTagger.tag(ObservationTags.ORDER_ID, id);
        orderService.deleteOrder(id);
        redirectAttributes.addFlashAttribute("message", "Order deleted");
        return "redirect:/ui/orders";
    }
}
