package com.winestoreapp.wineryadminui.features.dashboard;

import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui")
@Slf4j
@RequiredArgsConstructor
public class DashboardUiController {

    private final SpanTagger spanTagger;

    @GetMapping("/dashboard")
    @Observed(name = ObservationNames.UI_DASHBOARD_VIEW)
    public String dashboard() {
        log.debug("Accessing dashboard page");
        spanTagger.tag("ui.page", "dashboard");
        return "dashboard/dashboard";
    }
}