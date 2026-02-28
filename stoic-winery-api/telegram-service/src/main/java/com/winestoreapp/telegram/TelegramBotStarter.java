package com.winestoreapp.telegram;

import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.telegram.impl.TelegramBotNotificationService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TelegramBotStarter implements ApplicationRunner {

    private final TelegramBotNotificationService telegramBotNotificationService;

    @Override
    @Observed(name = ObservationNames.TELEGRAM_START)
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting Telegram Bot registration process...");
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotNotificationService);
            log.info("Telegram Bot registered and started successfully.");
        } catch (TelegramApiException e) {
            log.error("CRITICAL: Failed to register Telegram Bot. Notifications will not work.", e);
        }
    }
}