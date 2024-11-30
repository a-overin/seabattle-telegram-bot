package com.aoverin.telegram;

import com.aoverin.models.CellStatus;
import com.aoverin.models.GameField;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ReplyKeyboardUtils {

    public static ReplyKeyboardMarkup getGameField(GameField gameField, boolean masked) {
        ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder<?, ?> markupBuilder = ReplyKeyboardMarkup.builder();
        gameField.field().forEach((letter, map) -> {
            KeyboardRow row = new KeyboardRow(8);
            map.forEach((number, cell) -> row.add(cellToButton(cell, letter.toString() + number.toString(), masked)));
            markupBuilder.keyboardRow(row);
        });

        return markupBuilder.build();
    }

    private static String cellToString(CellStatus cell, String cellName) {
        return switch (cell) {
            case EMPTY -> cellName;
            case SHIP -> "⛵";
            case FIRED -> "\uD83D\uDD34";
            case FIRED_SHIP -> "❌";
        };
    }

    private static String cellToStringMasked(CellStatus cell, String cellName) {
        return switch (cell) {
            case EMPTY, SHIP -> cellName;
            case FIRED -> "\uD83D\uDD34";
            case FIRED_SHIP -> "❌";
        };
    }

    private static KeyboardButton cellToButton(CellStatus cell, String cellName, boolean masked) {
        var s = masked ? cellToStringMasked(cell, cellName) : cellToString(cell, cellName);
        return KeyboardButton.builder()
                .text(s)
                .build();
    }

    public static ByteArrayInputStream drawField(GameField gameField) {
        int cellSize = 30;
        int fieldSize = cellSize * 10; // assuming 10x10 field

        BufferedImage image = new BufferedImage(fieldSize, fieldSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, fieldSize, fieldSize);

        // Draw cells based on their status
        for (Map.Entry<Character, Map<Character, CellStatus>> row : gameField.field().entrySet()) {
            char rowChar = row.getKey();
            for (Map.Entry<Character, CellStatus> cell : row.getValue().entrySet()) {
                char colChar = cell.getKey();
                CellStatus status = cell.getValue();

                int rowIndex = rowChar - 'A';
                int colIndex = colChar - '0'; // assuming columns are numbered from '1' to '10'

                int x = colIndex * cellSize;
                int y = rowIndex * cellSize;
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x, y, cellSize, cellSize);
                switch (status) {
                    case SHIP -> g2d.setColor(Color.MAGENTA);
                    case FIRED, FIRED_SHIP -> g2d.setColor(Color.RED);
                    case EMPTY -> g2d.setColor(Color.BLACK);
                }
                var text = cellToString(status, rowChar + String.valueOf(colChar));

                g2d.drawString(text,  x + cellSize / 2 - 6, y +  cellSize / 2 + 5);

            }
        }

        // Draw grid
        g2d.setColor(Color.BLACK);
        for (int i = 0; i <= 10; i++) {
            g2d.drawLine(i * cellSize, 0, i * cellSize, fieldSize);
            g2d.drawLine(0, i * cellSize, fieldSize, i * cellSize);
        }

        g2d.dispose();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
