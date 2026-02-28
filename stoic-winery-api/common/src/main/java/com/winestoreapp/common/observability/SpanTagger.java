package com.winestoreapp.common.observability;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpanTagger {
    private final Tracer tracer;

    public void tag(String key, Object value) {
        if (value != null && tracer.currentSpan() != null) {
            tracer.currentSpan().tag(key, String.valueOf(value));
        }
    }
}
