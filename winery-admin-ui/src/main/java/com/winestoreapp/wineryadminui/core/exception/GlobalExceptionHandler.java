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
        redirectAttributes.addFlashAttribute("error", "The requested object was not found on the server.");
        return "redirect:/ui/wines";
    }

    @ExceptionHandler(FeignException.class)
    @Observed(name = "exception.feign")
    public String handleFeignException(FeignException e, RedirectAttributes redirectAttributes) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("exception.url", e.request().url());
        }
        log.error("Backend communication failed: Status={}, Method={}, URL={}, Body={}",
                e.status(), e.request().httpMethod(), e.request().url(), e.contentUTF8());

        redirectAttributes.addFlashAttribute("error", "Server communication error: " + e.status());
        return "redirect:/ui/wines";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Unexpected UI Error: ", e);
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        return "redirect:/ui/wines";
    }
}