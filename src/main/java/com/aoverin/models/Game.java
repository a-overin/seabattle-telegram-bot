package com.aoverin.models;

import java.util.Map;
import java.util.UUID;

public record Game(
        UUID id,
        Map<Long, GameField> fields,
        MoveOrder move
) {
    public Long getLooser() {
        return fields.entrySet().stream()
            .filter(e -> checkField(e.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    private boolean checkField(GameField field) {
        Map<Integer, Integer> countShips = field.countShips(CellStatus.FIRED_SHIP);
        return countShips.size() == 4 && countShips.entrySet().stream().allMatch(e -> 5 - e.getKey() == e.getValue());
    }
}
