package com.winestoreapp.wineryadminui.core.observability;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpanTagger {

    private final Tracer tracer;

    public void tag(String key, Object value) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }

        var span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, String.valueOf(value));
        }
    }

    public void error(Throwable throwable) {
        if (throwable == null) {
            return;
        }

        var span = tracer.currentSpan();
        if (span != null) {
            span.error(throwable);
        }
    }

    public void event(String name) {
        if (name == null || name.isBlank()) {
            return;
        }

        var span = tracer.currentSpan();
        if (span != null) {
            span.event(name);
        }
    }

    public String traceId() {
        var span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            return span.context().traceId();
        }
        return "N/A";
    }
}
