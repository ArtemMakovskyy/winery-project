package com.winestoreapp.telegram;

public interface NotificationService {
    boolean sendNotification(String message, Long recipientId);
}
