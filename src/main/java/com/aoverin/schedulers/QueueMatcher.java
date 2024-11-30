package com.aoverin.schedulers;


import com.aoverin.Main;
import com.aoverin.models.ChatQueueInfo;
import com.aoverin.models.Game;
import com.aoverin.models.UserState;
import com.aoverin.services.ChatService;
import com.aoverin.services.GameService;
import com.aoverin.services.QueueService;
import com.aoverin.telegram.TelegramBot;

public class QueueMatcher implements Runnable {

    private final TelegramBot bot;
    private final ChatService chatService;
    private final GameService gameService;
    private final QueueService queueService;

    public QueueMatcher(TelegramBot bot, Main.ClassHelper helper) {
        this.bot = bot;
        this.chatService = helper.chatService();
        this.gameService = helper.gameService();
        this.queueService = helper.queueService();
    }

    @Override
    public void run() {
        ChatQueueInfo user = queueService.getAndRemoveUser();
        if (user != null) {
            ChatQueueInfo opponent = getOpponent(user.userLogin());
            startGame(user, opponent);
        }
    }

    private void startGame(ChatQueueInfo user, ChatQueueInfo opponent) {
        if (opponent == null) {
            queueService.addUserToQueue(user.userId(), user.userLogin());
            return;
        }
        Game game = gameService.startGame(user, opponent);
        chatService.setChatState(user.userId(), UserState.IN_GAME);
        chatService.setChatState(opponent.userId(), UserState.IN_GAME);
        bot.sendTurns(game);
    }

    private ChatQueueInfo getOpponent(String userLogin) {
        return queueService.getAndRemoveUser();
    }
}
