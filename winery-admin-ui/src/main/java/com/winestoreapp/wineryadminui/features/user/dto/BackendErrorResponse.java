package com.winestoreapp.wineryadminui.features.user.dto;

public record BackendErrorResponse(
        String timestamp,
        int status,
        String error,
        String message
) {}