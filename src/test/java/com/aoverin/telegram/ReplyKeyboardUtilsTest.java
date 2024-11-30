package com.aoverin.telegram;

import com.aoverin.models.CellStatus;
import com.aoverin.models.GameField;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ReplyKeyboardUtilsTest {
    @Test
    public void test() throws IOException {
        final char[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
        Map<Character, Map<Character, CellStatus>> map = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            Map<Character, CellStatus> temp = new HashMap<>(10);
            for (int j = 0; j < 10; j++) {
                temp.put(String.valueOf(j).charAt(0), CellStatus.SHIP);
            }
            map.put(letters[i], temp);
        }
        var f = new GameField(map);
        ByteArrayInputStream byteArrayInputStream = ReplyKeyboardUtils.drawField(f);
        IOUtils.copy(byteArrayInputStream, new FileOutputStream("file.png"));
    }

    @Test
    public void testCheckWinner() {
        final char[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
        Map<Character, Map<Character, CellStatus>> map = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            Map<Character, CellStatus> temp = new HashMap<>(10);
            for (int j = 0; j < 10; j++) {
                temp.put(String.valueOf(j).charAt(0), CellStatus.FIRED);
            }
            map.put(letters[i], temp);
        }
//        // ****
//        map.get('A').put('0', CellStatus.FIRED_SHIP);
//        map.get('A').put('1', CellStatus.FIRED_SHIP);
//        map.get('A').put('2', CellStatus.FIRED_SHIP);
//        map.get('A').put('3', CellStatus.FIRED_SHIP);
//
//        // ***
//        map.get('A').put('7', CellStatus.FIRED_SHIP);
//        map.get('A').put('8', CellStatus.FIRED_SHIP);
//        map.get('A').put('9', CellStatus.FIRED_SHIP);
//
//        map.get('A').put('5', CellStatus.FIRED_SHIP);
//        map.get('B').put('5', CellStatus.FIRED_SHIP);
//        map.get('C').put('5', CellStatus.FIRED_SHIP);
//
//        // **
//        map.get('C').put('0', CellStatus.FIRED_SHIP);
//        map.get('C').put('1', CellStatus.FIRED_SHIP);
//
//        map.get('C').put('8', CellStatus.FIRED_SHIP);
//        map.get('C').put('9', CellStatus.FIRED_SHIP);
//
//        map.get('C').put('3', CellStatus.FIRED_SHIP);
//        map.get('D').put('3', CellStatus.FIRED_SHIP);
//
//        // *
//        map.get('H').put('0', CellStatus.FIRED_SHIP);
//
//        map.get('H').put('6', CellStatus.FIRED_SHIP);
//
//        map.get('H').put('9', CellStatus.FIRED_SHIP);
//
//        map.get('J').put('2', CellStatus.FIRED_SHIP);

        var field = new GameField(map);
        Map<Integer, Integer> countShips = field.countShips(CellStatus.FIRED_SHIP);
        System.out.println(countShips.size() == 4 && countShips.entrySet().stream().allMatch(e -> 5 - e.getKey() == e.getValue()));
    }
}