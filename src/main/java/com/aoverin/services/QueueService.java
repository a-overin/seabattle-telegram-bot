package com.aoverin.services;

import com.aoverin.models.ChatQueueInfo;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class QueueService {

    private final ConcurrentMap<Long, ChatQueueInfo> userQueue = new ConcurrentHashMap<>();

    public void addUserToQueue(Long chatId, String userLogin) {
        userQueue.put(chatId, new ChatQueueInfo(chatId, userLogin));
    }

    public ChatQueueInfo getAndRemoveUser() {
        Optional<ChatQueueInfo> info = userQueue.values().stream().findAny();
        info.ifPresent(e -> userQueue.remove(e.userId()));
        return info.orElse(null);
    }
}
