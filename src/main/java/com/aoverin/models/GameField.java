package com.aoverin.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public record GameField(
        Map<Character, Map<Character, CellStatus>> field
) {
    public boolean isValidConfiguration(boolean fin) {
        Set<Point> visited = new HashSet<>();
        Map<Integer, Integer> shipCount = new HashMap<>();
        shipCount.put(1, 4); // 4 одноклеточных корабля
        shipCount.put(2, 3); // 3 двухклеточных корабля
        shipCount.put(3, 2); // 2 трёхклеточных корабля
        shipCount.put(4, 1); // 1 четырёхклеточных корабль

        Map<Integer, Integer> maxShipCount = new HashMap<>();
        maxShipCount.put(1, 4); // 4 одноклеточных корабля
        maxShipCount.put(2, 3); // 3 двухклеточных корабля
        maxShipCount.put(3, 2); // 2 трёхклеточных корабля
        maxShipCount.put(4, 1); // 1 четырёхклеточных корабль

        for (Map.Entry<Character, Map<Character, CellStatus>> row : field.entrySet()) {
            for (Map.Entry<Character, CellStatus> cell : row.getValue().entrySet()) {
                char x = row.getKey();
                char y = cell.getKey();
                if (cell.getValue() == CellStatus.SHIP && !visited.contains(new Point(x, y))) {
                    List<Point> shipCells = new ArrayList<>();
                    if (!findShip(x, y, shipCells, visited)) return false;

                    int shipSize = shipCells.size();
                    if (!shipCount.containsKey(shipSize) || shipCount.get(shipSize) <= 0) {
                        return false; // неверное количество кораблей данной длины
                    }
                    shipCount.put(shipSize, shipCount.get(shipSize) - 1);
                }
            }
        }

        // проверка, что все корабли нужного количества
        if (fin) {
            return shipCount.values().stream().allMatch(count -> count == 0);
        } else {
            return shipCount.entrySet().stream().allMatch(entry -> entry.getValue() >= 0 && entry.getValue() <= maxShipCount.get(entry.getKey()));
        }
    }

    private boolean findShip(char x, char y, List<Point> shipCells, Set<Point> visited) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));
        visited.add(new Point(x, y));

        while (!queue.isEmpty()) {
            Point point = queue.poll();
            if (shipCells.contains(point)) continue;

            shipCells.add(point);

            for (Point neighbour : getNeighbours(point)) {
                if (!visited.contains(neighbour) && isShip(neighbour)) {
                    queue.add(neighbour);
                    visited.add(neighbour);
                }
            }
        }

        return isStraightLine(shipCells) && !hasAdjacentShips(shipCells, visited);
    }

    private List<Point> getNeighbours(Point point) {
        return List.of(
                new Point((char) (point.x + 1), point.y),
                new Point((char) (point.x - 1), point.y),
                new Point(point.x, (char) (point.y + 1)),
                new Point(point.x, (char) (point.y - 1))
        );
    }

    private List<Point> getNeighboursCheck(Point point) {
        return List.of(
                new Point((char) (point.x + 1), point.y),
                new Point((char) (point.x - 1), point.y),
                new Point(point.x, (char) (point.y + 1)),
                new Point(point.x, (char) (point.y - 1)),

                new Point((char) (point.x + 1), (char) (point.y + 1)),
                new Point((char) (point.x - 1), (char) (point.y - 1)),
                new Point((char) (point.x - 1), (char) (point.y + 1)),
                new Point((char) (point.x + 1), (char) (point.y - 1))
        );
    }

    private boolean isShip(Point p) {
        return field.containsKey(p.x) && field.get(p.x).containsKey(p.y)
                && field.get(p.x).get(p.y) == CellStatus.SHIP;
    }

    private boolean isStraightLine(List<Point> shipCells) {
        // проверка, что все точки в одной линии
        boolean sameRow = shipCells.stream().allMatch(p -> p.x == shipCells.get(0).x);
        boolean sameColumn = shipCells.stream().allMatch(p -> p.y == shipCells.get(0).y);
        return sameRow || sameColumn;
    }

    private boolean hasAdjacentShips(List<Point> shipCells, Set<Point> visited) {
        for (Point point : shipCells) {
            for (Point neighbour : getNeighboursCheck(point)) {
                if (!visited.contains(neighbour) && isShip(neighbour)) {
                    return true; // есть соседние корабли
                }
            }
        }
        return false;
    }

    private static class Point {
        char x;
        char y;

        Point(char x, char y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public Map<Integer, Integer> countShips(CellStatus findCellStatus) {
        Set<String> visited = new HashSet<>();
        Map<Integer, Integer> shipCounts = new HashMap<>();

        for (Map.Entry<Character, Map<Character, CellStatus>> rowEntry : field.entrySet()) {
            char row = rowEntry.getKey();
            for (Map.Entry<Character, CellStatus> columnEntry : rowEntry.getValue().entrySet()) {
                char col = columnEntry.getKey();
                CellStatus cellStatus = columnEntry.getValue();

                if ((cellStatus == CellStatus.SHIP || cellStatus == CellStatus.FIRED_SHIP) && !visited.contains("" + row + col)) {
                    List<CellStatus> shipCells = dfs(row, col, visited, findCellStatus);
                    if (shipCells.stream().allMatch(e -> e == findCellStatus)) {
                        shipCounts.put(shipCells.size(), shipCounts.getOrDefault(shipCells.size(), 0) + 1);
                    }
                }
            }
        }
        return shipCounts;
    }

    // Просмотр соседей для поиска корабля
    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    private List<CellStatus> dfs(char row, char col, Set<String> visited, CellStatus findCellStatus) {
        Stack<String> stack = new Stack<>();
        stack.push("" + row + col);
        List<CellStatus> result = new ArrayList<>();

        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!visited.contains(current)) {
                visited.add(current);
                result.add(field.get(current.charAt(0)).get(current.charAt(1)));

                for (int[] direction : DIRECTIONS) {
                    char newRow = (char) (current.charAt(0) + direction[0]);
                    char newCol = (char) (current.charAt(1) + direction[1]);
                    CellStatus cellStatus = field.getOrDefault(newRow, Collections.emptyMap()).getOrDefault(newCol, CellStatus.EMPTY);
                    if (isInBounds(newRow, newCol) && (cellStatus == CellStatus.SHIP || cellStatus == CellStatus.FIRED_SHIP) && !visited.contains("" + newRow + newCol)) {
                        stack.push("" + newRow + newCol);
                    }
                }
            }
        }
        return result;
    }

    private boolean isInBounds(char row, char col) {
        return field.containsKey(row) && field.get(row).containsKey(col);
    }
}
