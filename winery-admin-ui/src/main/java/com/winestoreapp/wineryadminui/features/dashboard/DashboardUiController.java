package com.winestoreapp.wineryadminui.features.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui")
@Slf4j
public class DashboardUiController {

    @GetMapping("/dashboard")
    public String dashboard() {
        log.debug("Accessing dashboard page");
        return "dashboard/dashboard";
    }
}