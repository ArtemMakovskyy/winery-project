package com.winestoreapp.telegram;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@Slf4j
public class TelegramBotCredentialProvider {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String token;

    @PostConstruct
    public void validateCredentials() {
        if (botName == null || botName.isEmpty() || token == null || token.isEmpty()) {
            log.error("Telegram Bot credentials are missing! Bot initialization may fail.");
        } else {
            log.info("Telegram Bot credentials for '{}' loaded successfully.", botName);
        }
    }
}
