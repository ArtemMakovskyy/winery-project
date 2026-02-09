package com.winestoreapp.wineryadminui.core.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(FeignException.NotFound.class)
    public String handleNotFound(FeignException.NotFound e, RedirectAttributes redirectAttributes) {
        log.warn("Resource not found on backend: URL={}", e.request().url());
        tagSpan("exception.type", "not_found");
        tagSpan("exception.url", e.request().url());

        redirectAttributes.addFlashAttribute("error", "The requested object was not found on the server.");
        return "redirect:/ui/wines";
    }

    @ExceptionHandler(FeignException.class)
    @Observed(name = "exception.feign")
    public String handleFeignException(FeignException e, RedirectAttributes redirectAttributes) {
        tagSpan("exception.url", e.request().url());
        tagSpan("exception.status", e.status());

        log.error("Backend communication failed: Status={}, Method={}, URL={}, Body={}",
                e.status(), e.request().httpMethod(), e.request().url(), e.contentUTF8());

        redirectAttributes.addFlashAttribute("error", "Server communication error: " + e.status());
        return "redirect:/ui/wines";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Unexpected UI Error: ", e);
        tagSpan("exception.message", e.getMessage());

        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Support ID: " + getTraceId());
        return "redirect:/ui/wines";
    }

    private void tagSpan(String key, Object value) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag(key, String.valueOf(value));
        }
    }

    private String getTraceId() {
        return tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "N/A";
    }
}