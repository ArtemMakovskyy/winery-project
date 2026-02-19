package com.winestoreapp.wineryadminui.core.exception;

import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private static final String DEFAULT_REDIRECT = "redirect:/ui/wines";
    private final SpanTagger spanTagger;
    private final FeignErrorParser feignErrorParser;

    @ExceptionHandler(FeignException.NotFound.class)
    public String handleNotFound(
            FeignException.NotFound e,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.warn("Resource not found: url={}", e.request().url());
        tagFeign(e, "feign_not_found");

        redirectAttributes.addFlashAttribute(
                "error",
                "The requested object was not found.");
        return determineRedirect(request);
    }

    @ExceptionHandler(FeignException.class)
    public String handleFeignException(
            FeignException e,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        String message = feignErrorParser.extractMessage(e);
        log.error("Backend error: status={}, message={}", e.status(), message);
        tagFeign(e, "feign_error");

        redirectAttributes.addFlashAttribute(
                "error", message + ". Support ID: " + spanTagger.traceId());
        return determineRedirect(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(
            Exception e,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.error("UI Application error", e);
        spanTagger.tag(ObservationTags.ERROR_TYPE, "internal_ui_error");
        spanTagger.error(e);

        redirectAttributes.addFlashAttribute(
                "error",
                "An unexpected error occurred. Trace ID: " + spanTagger.traceId());
        return determineRedirect(request);
    }

    private void tagFeign(FeignException e, String type) {
        spanTagger.tag(ObservationTags.ERROR_TYPE, type);
        spanTagger.tag(ObservationTags.ERROR_STATUS, e.status());
        spanTagger.tag(ObservationTags.ERROR_URL, e.request().url());
        spanTagger.error(e);
    }

    private String determineRedirect(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return (referer != null && referer.contains("/ui/")) ? "redirect:" + referer : DEFAULT_REDIRECT;
    }
}