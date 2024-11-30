package com.aoverin.services;

import com.aoverin.models.UserState;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatService {

    private final ConcurrentMap<Long, UserState> chat2State = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, UUID> chat2Game = new ConcurrentHashMap<>();

    public UserState getChatState(Long chatId) {
        return chat2State.getOrDefault(chatId, UserState.IDLE);
    }

    public UUID getGameId(Long chatId) {
        return chat2Game.get(chatId);
    }

    public void setChatState(Long chatId, UserState state) {
        chat2State.put(chatId, state);
    }

    public void setChatGame(Long chatId, UUID game) {
        chat2Game.put(chatId, game);
    }
}
