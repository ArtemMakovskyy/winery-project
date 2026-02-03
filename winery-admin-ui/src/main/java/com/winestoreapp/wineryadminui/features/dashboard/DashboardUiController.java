// DashboardUiController.java
package com.winestoreapp.wineryadminui.features.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/ui")
@Slf4j
@RequiredArgsConstructor
public class DashboardUiController {

    private final Tracer tracer;

    @GetMapping("/dashboard")
    @Observed(name = "ui.dashboard.view")
    public String dashboard() {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("ui.page", "dashboard");
        }
        log.debug("Accessing dashboard page");
        return "dashboard/dashboard";
    }
}
