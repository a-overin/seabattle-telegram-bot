package com.aoverin;

import com.aoverin.schedulers.QueueMatcher;
import com.aoverin.services.ChatService;
import com.aoverin.services.GameService;
import com.aoverin.services.QueueService;
import com.aoverin.telegram.TelegramBot;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public record ClassHelper(
       ChatService chatService,
       GameService gameService,
       QueueService queueService
    ) {};

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        try {
            var helper = new ClassHelper(
                    new ChatService(),
                    new GameService(),
                    new QueueService()
            );
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            String botToken = args[0];
            String botName = args[1];
            Long adminId = Long.parseLong(args[2]);
            TelegramBot bot = new TelegramBot(botToken, botName, helper, adminId);
            QueueMatcher queueMatcher = new QueueMatcher(bot, helper);
            executor.scheduleAtFixedRate(queueMatcher, 5, 3, TimeUnit.SECONDS);
            botsApplication.registerBot(botToken, bot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}