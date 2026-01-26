package com.winestoreapp.wineryadminui.core.exception;

import lombok.extern.slf4j.Slf4j;
import feign.FeignException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.NotFound.class)
    public String handleNotFound(FeignException.NotFound e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "The requested object was not found on the server.");
        return "redirect:/ui/wines";
    }

    @ExceptionHandler(FeignException.class)
    public String handleFeignException(FeignException e, RedirectAttributes redirectAttributes) {
        log.error("Feign client error: Status {}, Method {}", e.status(), e.request().httpMethod());
        redirectAttributes.addFlashAttribute("error", "Server communication error: " + e.status());
        return "redirect:/ui/wines";
    }
}