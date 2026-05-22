package gui;

import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;

public class GameProgressManager {
    private static final String PROGRESS_FILE = "game_progress.json";

    public static class GameState {
        public boolean hasMaze;
        public int mazeWidth;
        public int mazeHeight;
        public String mazeWallsData;

        public int mazeStartX, mazeStartY;
        public int mazeEndX, mazeEndY;

        public double robotX;
        public double robotY;
        public double robotDir;
        public boolean gameRunning;
    }

    /**
     * Сохраняет текущее состояние лабиринта и робота из GameVisualizer
     */
    public static void saveProgress(GameVisualizer visualizer) {
        if (visualizer == null) return;

        try (FileWriter writer = new FileWriter(PROGRESS_FILE)) {
            GameState s = new GameState();
            s.robotX = visualizer.getRobotX();
            s.robotY = visualizer.getRobotY();
            s.robotDir = visualizer.getRobotDir();
            s.gameRunning = visualizer.isGameRunning();

            Maze maze = visualizer.getCurrentMaze();
            s.hasMaze = (maze != null);

            if (s.hasMaze) {
                s.mazeWidth = maze.getWidth();
                s.mazeHeight = maze.getHeight();
                s.mazeStartX = maze.start.x;
                s.mazeStartY = maze.start.y;
                s.mazeEndX = maze.end.x;
                s.mazeEndY = maze.end.y;

                StringBuilder sb = new StringBuilder();
                for (int y = 0; y < s.mazeHeight; y++) {
                    for (int x = 0; x < s.mazeWidth; x++) {
                        sb.append(maze.walls[y][x] ? "1" : "0");
                    }
                }
                s.mazeWallsData = sb.toString();
            } else {
                s.mazeWallsData = "";
                s.mazeWidth = 0; s.mazeHeight = 0;
                s.mazeStartX = 0; s.mazeStartY = 0;
                s.mazeEndX = 0;   s.mazeEndY = 0;
            }

            // ИСПРАВЛЕНО: Явно форматируем JSON с US-локалью, чтобы числа ВСЕГДА писались через точку
            String json = String.format(Locale.US,
                    "{\n" +
                            "  \"robot\": [ %.4f, %.4f, %.4f, %b ],\n" +
                            "  \"maze\": [ %b, %d, %d, \"%s\", %d, %d, %d, %d ]\n" +
                            "}",
                    s.robotX, s.robotY, s.robotDir, s.gameRunning,
                    s.hasMaze, s.mazeWidth, s.mazeHeight, s.mazeWallsData, s.mazeStartX, s.mazeStartY, s.mazeEndX, s.mazeEndY
            );

            writer.write(json);
        } catch (Exception e) {
            System.err.println("Ошибка сохранения прогресса: " + e.getMessage());
        }
    }

    /**
     * Загружает сохраненное состояние игры из файла PROGRESS_FILE
     */
    public static GameState loadProgress() {
        File file = new File(PROGRESS_FILE);
        if (!file.exists()) return null;

        try {
            String content = new String(Files.readAllBytes(Paths.get(PROGRESS_FILE)));
            GameState state = new GameState();

            // ИСПРАВЛЕНО: Безопасное чтение робота через Scanner без привязки к индексам строк
            String robotData = extractArrayContent(content, "robot");
            Scanner robotScanner = new Scanner(robotData).useLocale(Locale.US).useDelimiter("[\\s,\\]\\[]+");
            if (robotScanner.hasNextDouble()) state.robotX = robotScanner.nextDouble();
            if (robotScanner.hasNextDouble()) state.robotY = robotScanner.nextDouble();
            if (robotScanner.hasNextDouble()) state.robotDir = robotScanner.nextDouble();
            if (robotScanner.hasNextBoolean()) state.gameRunning = robotScanner.nextBoolean();
            robotScanner.close();

            // ИСПРАВЛЕНО: Безопасное чтение лабиринта
            String mazeData = extractArrayContent(content, "maze");
            Scanner mazeScanner = new Scanner(mazeData).useLocale(Locale.US).useDelimiter("[\\s,\\]\\[]+");
            if (mazeScanner.hasNextBoolean()) state.hasMaze = mazeScanner.nextBoolean();
            if (mazeScanner.hasNextInt()) state.mazeWidth = mazeScanner.nextInt();
            if (mazeScanner.hasNextInt()) state.mazeHeight = mazeScanner.nextInt();

            if (mazeScanner.hasNext()) {
                // Достаем строку стен и очищаем от лишних кавычек JSON
                state.mazeWallsData = mazeScanner.next().replace("\"", "");
            }

            if (mazeScanner.hasNextInt()) state.mazeStartX = mazeScanner.nextInt();
            if (mazeScanner.hasNextInt()) state.mazeStartY = mazeScanner.nextInt();
            if (mazeScanner.hasNextInt()) state.mazeEndX = mazeScanner.nextInt();
            if (mazeScanner.hasNextInt()) state.mazeEndY = mazeScanner.nextInt();
            mazeScanner.close();

            return state;
        } catch (Exception e) {
            System.err.println("Ошибка загрузки прогресса: " + e.getMessage());
            return null;
        }
    }

    /**
     * Применяет загруженный прогресс к GameVisualizer
     */
    public static void applyProgress(GameState state, GameVisualizer visualizer) {
        if (state == null || visualizer == null) return;

        if (state.hasMaze && state.mazeWallsData != null && !state.mazeWallsData.isEmpty()) {
            Maze restoredMaze = Maze.restoreFromProfile(
                    state.mazeWidth, state.mazeHeight, state.mazeWallsData,
                    state.mazeStartX, state.mazeStartY, state.mazeEndX, state.mazeEndY
            );
            visualizer.restoreGameState(restoredMaze, state.robotX, state.robotY, state.robotDir, state.gameRunning);
        } else {
            visualizer.restoreGameState(null, state.robotX, state.robotY, state.robotDir, state.gameRunning);
        }
    }

    // Вспомогательный хелпер для вырезания внутренностей квадратных скобок [...]
    private static String extractArrayContent(String json, String key) {
        int start = json.indexOf("\"" + key + "\": [") + key.length() + 5;
        int end = json.indexOf("]", start);
        return json.substring(start, end);
    }

    public static boolean hasProgress() { return new File(PROGRESS_FILE).exists(); }
    public static void deleteProgress() { new File(PROGRESS_FILE).delete(); }
}
