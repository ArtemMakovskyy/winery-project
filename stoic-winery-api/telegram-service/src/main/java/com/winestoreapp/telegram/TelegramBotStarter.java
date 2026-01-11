package com.winestoreapp.telegram;

import com.winestoreapp.telegram.impl.TelegramBotNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class TelegramBotStarter implements ApplicationRunner {

    private final TelegramBotNotificationService telegramBotNotificationService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotNotificationService);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to register telegram bot", e);
        }
    }
}