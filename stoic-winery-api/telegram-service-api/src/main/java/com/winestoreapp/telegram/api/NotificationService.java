package com.winestoreapp.telegram.api;

public interface NotificationService {
    boolean sendNotification(String message, Long recipientId);
}
