package com.winestoreapp.wineryadminui.features.health;

import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WineHealthService {

    private final WineHealthCheckFeignClient wineHealthCheckFeignClient;
    private final SpanTagger spanTagger;

    @PostConstruct
    @Observed(name = ObservationNames.HEALTH_INIT)
    public void init() {
        check();
    }

    @Observed(name = ObservationNames.HEALTH_CHECK)
    public void check() {
        try {
            wineHealthCheckFeignClient.healthCheck();
            spanTagger.tag(ObservationTags.STATUS, "UP");
        } catch (Exception e) {
            spanTagger.tag(ObservationTags.STATUS, "DOWN");
            spanTagger.error(e);
            log.error("CRITICAL: Failed to connect to WINE-STORE-SERVICE");
        }
    }
}