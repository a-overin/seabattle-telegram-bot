package com.aoverin.telegram;

import com.aoverin.models.CellStatus;
import com.aoverin.models.GameField;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Map;

public class MessagesUtils {

    public static SendMessage cancelMessage(Long chatId) {
        return SendMessage
            .builder()
            .replyMarkup(new ReplyKeyboardRemove(true))
            .chatId(chatId)
            .text("Игра отменена")
            .build();
    }

    public static SendMessage prepareGame(Long chatId, GameField gameField) {
        Map<Integer, Integer> count = gameField.countShips(CellStatus.SHIP);
        var message = "Расставь корабли, осталось: " +
                "****: " + (1 - count.getOrDefault(4, 0)) +
                " ***: " + (2 - count.getOrDefault(3, 0)) +
                " **: " + (3 - count.getOrDefault(2, 0)) +
                " *:" + (4 - count.getOrDefault(1, 0));
        if ((1 - count.getOrDefault(4, 0)) + (2 - count.getOrDefault(3, 0)) + (3 - count.getOrDefault(2, 0)) + (4 - count.getOrDefault(1, 0)) == 0) {
            message = message + "\r\n/find - для поиска игры";
        }
        return SendMessage
            .builder()
            .replyMarkup(ReplyKeyboardUtils.getGameField(gameField, false))
            .chatId(chatId)
            .text(message)
            .build();
    }

    public static SendPhoto addToQueue(Long chaId, GameField gameField) {
        return SendPhoto.builder()
            .chatId(chaId)
            .photo(new InputFile(ReplyKeyboardUtils.drawField(gameField), "field.png"))
            .replyMarkup(new ReplyKeyboardRemove(true))
            .caption("Начался поиск соперника для игры!")
            .build();
    }

    public static SendPhoto waitTurnMessage(Long chaId, GameField gameField) {
        return SendPhoto.builder()
            .chatId(chaId)
            .photo(new InputFile(ReplyKeyboardUtils.drawField(gameField), "field.png"))
            .replyMarkup(new ReplyKeyboardRemove(true))
            .caption("Ход соперника")
            .build();
    }

    public static SendPhoto makeMove(Long chatId, GameField chatGameField, GameField opponentGameField) {
        return SendPhoto.builder()
            .chatId(chatId)
            .photo(new InputFile(ReplyKeyboardUtils.drawField(chatGameField), "field.png"))
            .replyMarkup(ReplyKeyboardUtils.getGameField(opponentGameField, true))
            .caption("Твой ход!")
            .build();
    }

    public static SendMessage finishGame(Long chatId, boolean isLooser) {
        return SendMessage
            .builder()
            .replyMarkup(new ReplyKeyboardRemove(true))
            .chatId(chatId)
            .text(isLooser ? "Ты проиграл!" : "Ты победил")
            .build();
    }

    public static SendPhoto fieldPhoto(Long chatId, GameField field) {
        return SendPhoto
            .builder()
            .photo(new InputFile(ReplyKeyboardUtils.drawField(field), "field.png"))
            .chatId(chatId)
            .build();
    }
}
