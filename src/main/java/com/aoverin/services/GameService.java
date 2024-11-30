package com.aoverin.services;

import com.aoverin.models.CellStatus;
import com.aoverin.models.ChatQueueInfo;
import com.aoverin.models.Game;
import com.aoverin.models.GameField;
import com.aoverin.models.MoveOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.abilitybots.api.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameService {

    private static final char[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final ConcurrentMap<Long, GameField> chat2Prepare = new ConcurrentHashMap<>();

    private final ConcurrentMap<UUID, Game> games = new ConcurrentHashMap<>();

    public GameField getInitField() {
        Map<Character, Map<Character, CellStatus>> map = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            Map<Character, CellStatus> temp = new HashMap<>(10);
            for (int j = 0; j < 10; j++) {
                temp.put(String.valueOf(j).charAt(0), CellStatus.EMPTY);
            }
            map.put(letters[i], temp);
        }
        // ****
        map.get('A').put('0', CellStatus.SHIP);
        map.get('A').put('1', CellStatus.SHIP);
        map.get('A').put('2', CellStatus.SHIP);
        map.get('A').put('3', CellStatus.SHIP);

        // ***
        map.get('A').put('7', CellStatus.SHIP);
        map.get('A').put('8', CellStatus.SHIP);
        map.get('A').put('9', CellStatus.SHIP);

        map.get('A').put('5', CellStatus.SHIP);
        map.get('B').put('5', CellStatus.SHIP);
        map.get('C').put('5', CellStatus.SHIP);

        // **
        map.get('C').put('0', CellStatus.SHIP);
        map.get('C').put('1', CellStatus.SHIP);

        map.get('C').put('8', CellStatus.SHIP);
        map.get('C').put('9', CellStatus.SHIP);

        map.get('C').put('3', CellStatus.SHIP);
        map.get('D').put('3', CellStatus.SHIP);

        // *
        map.get('H').put('0', CellStatus.SHIP);

        map.get('H').put('6', CellStatus.SHIP);

        map.get('H').put('9', CellStatus.SHIP);

        map.get('J').put('2', CellStatus.SHIP);
        return new GameField(map);
    }

    public void savePrepareField(Long chatId, GameField field) {
        chat2Prepare.put(chatId, field);
    }

    public GameField updateField(Long chatId, char letter, char number) {
        GameField field = chat2Prepare.get(chatId);
        field.field().get(letter).compute(number, (k, v) -> (v == CellStatus.EMPTY) ? CellStatus.SHIP : CellStatus.EMPTY);
        if (!field.isValidConfiguration(false)) {
            field.field().get(letter).compute(number, (k, v) -> (v == CellStatus.EMPTY) ? CellStatus.SHIP : CellStatus.EMPTY);
        }
        return field;
    }

    public GameField getChatField(Long chatId) {
        return chat2Prepare.get(chatId);
    }

    public Game startGame(ChatQueueInfo user, ChatQueueInfo opponent) {
        GameField userField = chat2Prepare.get(user.userId());
        GameField opponentField = chat2Prepare.get(opponent.userId());
        Game game = new Game(
                UUID.randomUUID(),
                Map.of(
                        user.userId(), userField,
                        opponent.userId(), opponentField
                ),
            new MoveOrder(user.userId(), opponent.userId())
        );
        games.put(game.id(), game);
        return game;
    }

    public Game abortGame(Long chatId) {
        Optional<Game> game = getGameBy(chatId);
        game.ifPresent(g -> games.remove(g.id()));
        return game.orElse(null);
    }

    public Game getGame(Long chatId) {
        return getGameBy(chatId).orElse(null);
    }

    public Game updateGameField(Long chatId, char letter, char number) {
        Game game = getGameBy(chatId).get();
        Map<Character, Map<Character, CellStatus>> field = game.fields().get(game.move().getOpponentChatId()).field();
        CellStatus curStatus = field.get(letter).get(number);
        if (curStatus == CellStatus.FIRED || curStatus == CellStatus.FIRED_SHIP) {
            return null;
        }
        switch (curStatus) {
            case EMPTY -> {
                field.get(letter).put(number, CellStatus.FIRED);
                setNextMoves(game);
            }
            case SHIP -> field.get(letter).put(number, CellStatus.FIRED_SHIP);
        }
        fillAroundShip(game.fields().get(chatId), letter, number);
        return game;
    }

    private void setNextMoves(Game game) {
        Long moverChatId = game.move().getMoverChatId();
        game.move().setMoverChatId(game.move().getOpponentChatId());
        game.move().setOpponentChatId(moverChatId);
    }

    private void fillAroundShip(GameField gameField, char letter, char number) {
        // todo закрашивать вокруг корабля
    }

    private Optional<Game> getGameBy(Long chatId) {
        return games.values().stream().filter(g -> g.fields().containsKey(chatId)).findFirst();
    }
}
