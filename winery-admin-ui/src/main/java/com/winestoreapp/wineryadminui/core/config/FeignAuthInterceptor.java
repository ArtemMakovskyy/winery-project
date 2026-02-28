package com.winestoreapp.wineryadminui.core.config;

import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@ConfigurationProperties(prefix = "feign.auth")
@Getter
@Setter
public class FeignAuthInterceptor implements RequestInterceptor {

    private List<String> skipUrls;
    private SessionTokenStorage storage;
    private SpanTagger spanTagger;

    public FeignAuthInterceptor() {
    }

    @Autowired
    public void setStorage(SessionTokenStorage storage) {
        this.storage = storage;
    }

    @Autowired
    public void setSpanTagger(SpanTagger spanTagger) {
        this.spanTagger = spanTagger;
    }

    @Override
    public void apply(RequestTemplate template) {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        var session = attributes.getRequest().getSession(false);
        if (session == null) {
            return;
        }

        String token = storage.get(session);

        boolean shouldSkip = skipUrls != null && skipUrls.stream().anyMatch(template.url()::contains);

        if (token != null && !shouldSkip) {
            template.header("Authorization", "Bearer " + token);
            spanTagger.tag(ObservationTags.AUTH_HEADERS_PRESENT, true);
            log.debug("Auth token attached to request: {}", template.url());
        }
    }

    @PostConstruct
    private void logLoadedUrls() {
        log.info("Feign skip URLs loaded: {}", skipUrls);
    }
}