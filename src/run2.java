import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class run2 {

    // Константа для хранения возможных направлений движения (вверх, вниз, влево, вправо).
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // Метод для чтения лабиринта из стандартного ввода.
    private static char[][] getInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }

        char[][] maze = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            maze[i] = lines.get(i).toCharArray();
        }

        return maze;
    }

    // Основной метод для решения задачи с использованием алгоритма поиска в ширину.
    private static int minStepsToCollectAllKeys(char[][] maze) {

        int mazeColumns = maze[0].length;
        List<int[]> robotStartPositions = new ArrayList<>();
        int totalKeysCount;

        // Находим стартовые позиции всех роботов ('@') и общее количество ключей (строчные буквы).
        int[] keyAndRobotInfo = getStartPositionsAndKeyCount(maze, robotStartPositions);
        totalKeysCount = keyAndRobotInfo[0];
        // robotStartPositions теперь содержит стартовые координаты роботов.

        // Проверяем, что роботов ровно 4
        if (robotStartPositions.size() != 4) {
            throw new IllegalArgumentException("Должно быть ровно 4 робота.");
        }

        // Очередь для состояний, используемая в поиске в ширину. Каждое состояние - это позиции роботов,
        // собранные ключи и количество шагов.
        Queue<State> stateQueue = new LinkedList<>();
        // Множество для хранения посещенных состояний. Используется для предотвращения
        // зацикливания и повторной обработки одинаковых состояний.
        Set<String> visitedStates = new HashSet<>();

        int initialKeysBitmask = 0; // Изначально ключи не собраны.
        int[] initialRobotPositionsLinear = new int[4]; // Позиции роботов в линейном представлении (row * cols + col).

        // Преобразуем стартовые 2D-координаты роботов в линейные индексы для удобства хранения в массиве.
        for (int i = 0; i < 4; i++) {
            initialRobotPositionsLinear[i] = robotStartPositions.get(i)[0] * mazeColumns + robotStartPositions.get(i)[1];
        }

        // Создаем и добавляем начальное состояние в очередь и множество посещенных состояний.
        State initialState = new State(initialRobotPositionsLinear, initialKeysBitmask, 0);
        stateQueue.offer(initialState);
        visitedStates.add(getStateKey(initialRobotPositionsLinear, initialKeysBitmask));

        // Запускаем алгоритм поиска в ширину.
        while (!stateQueue.isEmpty()) {
            State currentState = stateQueue.poll(); // Извлекаем текущее состояние из очереди.
            int currentSteps = currentState.getSteps();
            int currentCollectedKeys = currentState.getCollectedKeys();
            int[] currentRobotPositions = currentState.getRobotPositions();

            // Если все ключи собраны, мы нашли кратчайший путь.
            if (areAllKeysCollected(currentCollectedKeys, totalKeysCount)) {
                return currentSteps;
            }

            // Пытаемся переместить каждого робота в каждом из 4 направлений.
            for (int robotIndex = 0; robotIndex < 4; robotIndex++) {
                int robotLinearPosition = currentRobotPositions[robotIndex];
                int robotRow = robotLinearPosition / mazeColumns;
                int robotColumn = robotLinearPosition % mazeColumns;

                // Перебираем все возможные направления движения.
                for (int[] direction : DIRECTIONS) {
                    int newRow = robotRow + direction[0];
                    int newColumn = robotColumn + direction[1];

                    // Проверяем, является ли новый ход допустимым (не стена, не выход за границы, дверь открыта).
                    if (isValidMove(maze, newRow, newColumn, currentCollectedKeys)) {

                        // Создаем новое состояние после перемещения робота.
                        State newState = getNewState(maze, currentState, robotIndex, newRow, newColumn);
                        String stateKey = getStateKey(newState.getRobotPositions(), newState.getCollectedKeys());

                        // Если это состояние ранее не посещалось, добавляем его в множество посещенных
                        // и в очередь для дальнейшей обработки.
                        if (!visitedStates.contains(stateKey)) {
                            visitedStates.add(stateKey);
                            stateQueue.offer(newState);
                        }
                    }
                }
            }
        }

        return Integer.MAX_VALUE;
    }

    // Вспомогательный метод для получения стартовых позиций роботов и подсчета ключей.
    private static int[] getStartPositionsAndKeyCount(char[][] maze, List<int[]> robotStartPositions) {
        int totalKeysCount = 0;
        int mazeRows = maze.length;
        int mazeColumns = maze[0].length;

        for (int row = 0; row < mazeRows; row++) {
            for (int column = 0; column < mazeColumns; column++) {
                if (maze[row][column] == '@') {
                    robotStartPositions.add(new int[]{row, column});
                } else if (Character.isLowerCase(maze[row][column])) {
                    totalKeysCount++;
                }
            }
        }

        return new int[]{totalKeysCount};
    }

    // Вспомогательный метод для создания нового состояния после хода робота.
    private static State getNewState(char[][] maze, State currentState, int robotIndex, int newRow, int newColumn) {
        int newKeysBitmask = currentState.getCollectedKeys();
        char cell = maze[newRow][newColumn];

        // Если новая клетка - ключ, обновляем битовую маску собранных ключей.
        if (Character.isLowerCase(cell)) {
            newKeysBitmask |= getKeyBitmask(cell);
        }

        // Копируем массив позиций роботов и обновляем позицию перемещенного робота.
        // Используем linearPosition = row * cols + col
        int mazeColumns = maze[0].length;
        int[] newRobotPositions = Arrays.copyOf(currentState.getRobotPositions(), currentState.getRobotPositions().length);
        newRobotPositions[robotIndex] = newRow * mazeColumns + newColumn;

        // Создаем и возвращаем новое состояние с увеличенным на 1 количеством шагов.
        return new State(newRobotPositions, newKeysBitmask, currentState.getSteps() + 1);
    }

    // Вспомогательный метод для проверки, собраны ли все ключи.
    // Сравнивает битовую маску собранных ключей с маской, где все биты установлены.
    private static boolean areAllKeysCollected(int collectedKeys, int totalKeysCount) {
        // Маска, где установлены биты с 0 по totalKeysCount-1.
        int goalBitmask = (1 << totalKeysCount) - 1;
        return collectedKeys == goalBitmask;
    }

    // Вспомогательный метод для проверки допустимости хода.
    private static boolean isValidMove(char[][] maze, int row, int column, int collectedKeys) {
        // Проверяем выход за границы лабиринта.
        if (row < 0 || row >= maze.length || column < 0 || column >= maze[0].length) {
            return false;
        }

        char cell = maze[row][column];

        // Проверяем, является ли клетка стеной.
        if (cell == '#') {
            return false;
        }

        // Проверяем, является ли клетка дверью (заглавная буква).
        if (Character.isUpperCase(cell)) {
            // Если это дверь, проверяем, есть ли у роботов соответствующий ключ.
            return isDoorOpen(cell, collectedKeys);
        }

        return true;
    }

    // Вспомогательный метод для получения битовой маски для данного ключа.
    private static int getKeyBitmask(char key) {
        // Убеждаемся, что это строчная буква.
        if (!Character.isLowerCase(key)) {
            throw new IllegalArgumentException("Неверный символ ключа: " + key);
        }
        return (1 << (key - 'a'));
    }

    // Вспомогательный метод для проверки, открыта ли дверь.
    private static boolean isDoorOpen(char door, int collectedKeys) {
        // Убеждаемся, что это заглавная буква.
        if (!Character.isUpperCase(door)) {
            throw new IllegalArgumentException("Неверный символ двери: " + door);
        }
        // Вычисляем битовую маску, соответствующую ключу для этой двери.
        int requiredKeyBitmask = (1 << (door - 'A'));
        // Проверяем, установлен ли соответствующий бит в маске собранных ключей.
        return (collectedKeys & requiredKeyBitmask) != 0;
    }

    // Вспомогательный метод для генерации строкового ключа состояния для использования в Set.
    // Этот ключ уникально идентифицирует состояние (позиции всех роботов + собранные ключи).
    private static String getStateKey(int[] robotPositions, int collectedKeys) {
        // Сортируем позиции роботов, чтобы порядок роботов в массиве не влиял на ключ состояния.
        // Это важно, потому что перестановка роботов местами в одной и той же клетке
        // не меняет сути состояния лабиринта.
        int[] sortedPositions = Arrays.copyOf(robotPositions, robotPositions.length);
        Arrays.sort(sortedPositions); // Сортируем скопированный массив.
        return Arrays.toString(sortedPositions) + "-" + collectedKeys;
    }


    // Внутренний статический класс для представления состояния в алгоритме.
    // Состояние включает позиции всех роботов, битовую маску собранных ключей
    // и количество шагов, затраченных для достижения этого состояния.
    static class State {
        private final int[] robotPositions; // Массив линейных позиций роботов (row * cols + col).
        private final int collectedKeys; // Битовая маска собранных ключей.
        private final int steps; // Количество шагов.

        State(int[] robotPositions, int collectedKeys, int steps) {
            this.robotPositions = Arrays.copyOf(robotPositions, robotPositions.length);
            this.collectedKeys = collectedKeys;
            this.steps = steps;
        }

        public int[] getRobotPositions() {
            return Arrays.copyOf(robotPositions, robotPositions.length);
        }

        public int getCollectedKeys() {
            return collectedKeys;
        }

        public int getSteps() {
            return steps;
        }
    }

    public static void main(String[] args) throws IOException {
        char[][] data = getInput();
        int result = minStepsToCollectAllKeys(data);

        if (result == Integer.MAX_VALUE) {
            System.out.println("No solution found");
        } else {
            System.out.println(result);
        }
    }
}