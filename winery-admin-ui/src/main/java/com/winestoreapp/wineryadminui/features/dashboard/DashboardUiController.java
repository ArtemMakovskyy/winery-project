package com.winestoreapp.wineryadminui.features.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui")
public class DashboardUiController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/dashboard";
    }
}
