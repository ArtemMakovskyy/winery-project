package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class OrderUiController {

    private final OrderService orderService;

    @GetMapping
    public String list(Model model) {
        List<OrderDto> orders = orderService.getAll();
        model.addAttribute("orders", orders);
        return "order/orders";
    }

    @PostMapping("/{id}/paid")
    public String setPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.setPaidStatus(id);
            redirectAttributes.addFlashAttribute("message", "Order marked as paid");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update status: " + e.getMessage());
        }
        return "redirect:/ui/orders";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "redirect:/ui/orders";
    }
}
