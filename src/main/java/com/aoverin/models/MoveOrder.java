package com.aoverin.models;

public class MoveOrder {
    private Long moverChatId;
    private Long opponentChatId;

    public MoveOrder(Long moverChatId, Long opponentChatId) {
        this.moverChatId = moverChatId;
        this.opponentChatId = opponentChatId;
    }

    public Long getMoverChatId() {
        return moverChatId;
    }

    public void setMoverChatId(Long moverChatId) {
        this.moverChatId = moverChatId;
    }

    public Long getOpponentChatId() {
        return opponentChatId;
    }

    public void setOpponentChatId(Long opponentChatId) {
        this.opponentChatId = opponentChatId;
    }
}
