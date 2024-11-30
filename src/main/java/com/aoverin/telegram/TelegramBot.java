package com.aoverin.telegram;

import com.aoverin.Main;
import com.aoverin.models.Game;
import com.aoverin.models.GameField;
import com.aoverin.models.UserState;
import com.aoverin.services.ChatService;
import com.aoverin.services.GameService;
import com.aoverin.services.QueueService;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

public class TelegramBot extends AbilityBot {

    private final ChatService chatService;
    private final GameService gameService;
    private final QueueService queueService;
    private final Long adminId;

    public TelegramBot(String botToken, String botUsername, Main.ClassHelper helper, Long adminId) {
        super(new OkHttpTelegramClient(botToken), botUsername);
        this.onRegister();
        this.gameService = helper.gameService();
        this.chatService = helper.chatService();
        this.queueService = helper.queueService();
        this.adminId = adminId;
    }

    @Override
    public long creatorId() {
        return adminId;
    }

    public Ability defaultMessage() {
        return Ability
                .builder()
                .name(DEFAULT)
                .info("default")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> silent.send(getMessageByChatState(ctx.update()), getChatId(ctx.update())))
                .build();
    }

    public Ability startMessage() {
        return Ability
            .builder()
            .name("start")
            .info("start")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .input(0)
            .action(ctx -> silent.send(getMessageByChatState(ctx.update()), getChatId(ctx.update())))
            .build();
    }

    public Ability stopMessage() {
        return Ability
                .builder()
                .name("stop")
                .info("cancel game")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> stopGame(ctx.update()))
                .build();
    }

    public Ability startPrepare() {
        return Ability
                .builder()
                .name("prepare")
                .info("prepare game")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> initGameField(ctx.update()))
                .build();
    }

    public Reply setShip() {
        return Reply.of((bot, upd) -> updatePrepareField(upd),
            hasStatus(UserState.PREPARE_GAME), hasMessageWithCell()
        );
    }

    public Ability startQueue() {
        return Ability
                .builder()
                .flag(hasStatus(UserState.PREPARE_GAME), readyToStartGame())
                .name("find")
                .info("find game")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> addToQueue(ctx.update(), null))
                .build();
    }

    public Reply fireShip() {
        return Reply.of((bot, upd) -> updateGameField(upd),
            hasStatus(UserState.IN_GAME), isUserTurn(), hasMessageWithCell()
        );
    }

    public void sendTurns(Game game) {
        try {
            getTelegramClient().execute(
                MessagesUtils.makeMove(
                    game.move().getMoverChatId(),
                    game.fields().get(game.move().getMoverChatId()),
                    game.fields().get(game.move().getOpponentChatId())
                )
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        game.fields().keySet().stream()
            .filter(e -> !Objects.equals(game.move().getMoverChatId(), e))
            .forEach(e -> {
                try {
                    getTelegramClient().execute(MessagesUtils.waitTurnMessage(e, game.fields().get(e)));
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
            });
    }

    private void addToQueue(Update upd, String userLogin) {
        queueService.addUserToQueue(getChatId(upd), userLogin);
        chatService.setChatState(getChatId(upd), UserState.FIND_GAME);
        try {
            getTelegramClient().execute(MessagesUtils.addToQueue(getChatId(upd), gameService.getChatField(getChatId(upd))));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void initGameField(Update upd) {
        chatService.setChatState(getChatId(upd), UserState.PREPARE_GAME);
        GameField field = gameService.getInitField();
        gameService.savePrepareField(getChatId(upd), field);
        silent.execute(MessagesUtils.prepareGame(getChatId(upd), field));
    }

    private void updatePrepareField(Update upd) {
        silent.execute(
            MessagesUtils.prepareGame(
                getChatId(upd),
                gameService.updateField(getChatId(upd), upd.getMessage().getText().charAt(0), upd.getMessage().getText().charAt(1))
            )
        );
    }

    private void updateGameField(Update upd) {
        Game game = gameService.updateGameField(getChatId(upd), upd.getMessage().getText().charAt(0), upd.getMessage().getText().charAt(1));
        Long looserId = game.getLooser();
        if (looserId != null) {
            game.fields().forEach((chatId, field) -> {
                chatService.setChatState(chatId, UserState.IDLE);
                // send opponents field to chat
                game.fields().entrySet().stream()
                    .filter(e -> !e.getKey().equals(chatId))
                    .map(Map.Entry::getValue)
                    .forEach(f -> {
                        try {
                            getTelegramClient().execute(MessagesUtils.fieldPhoto(chatId, f));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
                // send result message  to chat
                silent.execute(MessagesUtils.finishGame(chatId, chatId.equals(looserId)));
            });
        } else {
            sendTurns(game);
        }
    }

    private void stopGame(Update upd) {
        Game game = gameService.abortGame(getChatId(upd));
        if (game != null) {
            game.fields().keySet().forEach(chatId -> {
                chatService.setChatState(chatId, UserState.IDLE);
                silent.execute(MessagesUtils.cancelMessage(chatId));
            });
        } else {
            chatService.setChatState(getChatId(upd), UserState.IDLE);
            silent.execute(MessagesUtils.cancelMessage(getChatId(upd)));
        }
    }

    private Predicate<Update> hasMessageWithCell() {
        return upd -> {
            String text = upd.getMessage().getText();
            return text.matches("[A-J][0-9]");
        };
    }

    private Predicate<Update> readyToStartGame() {
        return upd -> {
            GameField chatField = gameService.getChatField(getChatId(upd));
            return chatField != null && chatField.isValidConfiguration(true);
        };
    }

    private Predicate<Update> hasMessageWith(String msg) {
        return upd -> upd.getMessage().getText().equalsIgnoreCase(msg);
    }

    private Predicate<Update> hasCommandWith(String name) {
        return upd -> upd.getMessage().getText().startsWith(format("%s%s", getCommandPrefix(), name));
    }

    private Predicate<Update> isUserTurn() {
        return upd -> {
            Game game = gameService.getGame(getChatId(upd));
            if (game == null) {
                return false;
            }
            return Objects.equals(game.move().getMoverChatId(), getChatId(upd));
        };
    }

    private Predicate<Update> hasStatus(UserState state) {
        return upd -> chatService.getChatState(getChatId(upd)) == state;
    }

    private String getMessageByChatState(Update update) {
        UserState state = chatService.getChatState(getChatId(update));
        switch (state) {
            case IDLE -> { return "Start prepare game /prepare"; }
            default -> { return "You are waiting your turn, use /stop for abort."; }
        }
    }
}
