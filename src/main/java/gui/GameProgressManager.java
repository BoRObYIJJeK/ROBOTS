package gui;

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

        // Попиксельные данные тумана
        public int difficulty;
        public String fogPixelsData;
    }

    public static void saveProgress(GameVisualizer visualizer) {
        if (visualizer == null) return;
        try (FileWriter writer = new FileWriter(PROGRESS_FILE)) {
            GameState s = new GameState();
            s.robotX = visualizer.getRobotX();
            s.robotY = visualizer.getRobotY();
            s.robotDir = visualizer.getRobotDir();
            s.gameRunning = visualizer.isGameRunning();
            s.difficulty = visualizer.getCurrentDifficulty();

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

                // Извлекаем попиксельную строку тумана войны
                if (visualizer.getFogOfWar() != null) {
                    s.fogPixelsData = visualizer.getFogOfWar().getFogPixelsString(s.difficulty);
                } else {
                    s.fogPixelsData = "";
                }
            } else {
                s.mazeWallsData = "";
                s.fogPixelsData = "";
                s.mazeWidth = 0; s.mazeHeight = 0;
                s.mazeStartX = 0; s.mazeStartY = 0; s.mazeEndX = 0; s.mazeEndY = 0;
            }

            // Записываем все в JSON в том же формате
            String json = String.format(Locale.US, "{\n" +
                            "  \"robot\": [ %.4f, %.4f, %.4f, %b ],\n" +
                            "  \"maze\": [ %b, %d, %d, \"%s\", %d, %d, %d, %d ],\n" +
                            "  \"fog\": [ %d, \"%s\" ]\n" +
                            "}",
                    s.robotX, s.robotY, s.robotDir, s.gameRunning,
                    s.hasMaze, s.mazeWidth, s.mazeHeight, s.mazeWallsData, s.mazeStartX, s.mazeStartY, s.mazeEndX, s.mazeEndY,
                    s.difficulty, s.fogPixelsData
            );
            writer.write(json);
        } catch (Exception e) {
            System.err.println("Ошибка сохранения прогресса: " + e.getMessage());
        }
    }

    public static GameState loadProgress() {
        File file = new File(PROGRESS_FILE);
        if (!file.exists()) return null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(PROGRESS_FILE)));
            GameState state = new GameState();

            // Чтение робота
            String robotData = extractArrayContent(content, "robot");
            Scanner robotScanner = new Scanner(robotData).useLocale(Locale.US).useDelimiter("[\\s,\\]\\[]+");
            if (robotScanner.hasNextDouble()) state.robotX = robotScanner.nextDouble();
            if (robotScanner.hasNextDouble()) state.robotY = robotScanner.nextDouble();
            if (robotScanner.hasNextDouble()) state.robotDir = robotScanner.nextDouble();
            if (robotScanner.hasNextBoolean()) state.gameRunning = robotScanner.nextBoolean();
            robotScanner.close();

            // Чтение лабиринта
            String mazeData = extractArrayContent(content, "maze");
            Scanner mazeScanner = new Scanner(mazeData).useLocale(Locale.US).useDelimiter("[\\s,\\]\\[]+");
            if (mazeScanner.hasNextBoolean()) state.hasMaze = mazeScanner.nextBoolean();
            if (mazeScanner.hasNextInt()) state.mazeWidth = mazeScanner.nextInt();
            if (mazeScanner.hasNextInt()) state.mazeHeight = mazeScanner.nextInt();
            if (mazeScanner.hasNext()) {
                state.mazeWallsData = mazeScanner.next().replace("\"", "");
            }
            if (mazeScanner.hasNextInt()) state.mazeStartX = mazeScanner.nextInt();
            if (mazeScanner.hasNextInt()) state.mazeStartY = mazeScanner.nextInt();
            if (mazeScanner.hasNextInt()) state.mazeEndX = mazeScanner.nextInt();
            if (mazeScanner.hasNextInt()) state.mazeEndY = mazeScanner.nextInt();
            mazeScanner.close();

            // Чтение попиксельного тумана
            if (content.contains("\"fog\":")) {
                String fogData = extractArrayContent(content, "fog");
                Scanner fogScanner = new Scanner(fogData).useLocale(Locale.US).useDelimiter("[\\s,\\]\\[]+");
                if (fogScanner.hasNextInt()) state.difficulty = fogScanner.nextInt();
                if (fogScanner.hasNext()) {
                    state.fogPixelsData = fogScanner.next().replace("\"", "").trim();
                }
                fogScanner.close();
            } else {
                state.difficulty = 0;
                state.fogPixelsData = "";
            }
            return state;
        } catch (Exception e) {
            System.err.println("Ошибка загрузки прогресса: " + e.getMessage());
            return null;
        }
    }

    public static void applyProgress(GameState state, GameVisualizer visualizer) {
        if (state == null || visualizer == null) return;
        if (state.hasMaze && state.mazeWallsData != null && !state.mazeWallsData.isEmpty()) {
            Maze restoredMaze = Maze.restoreFromProfile(
                    state.mazeWidth, state.mazeHeight, state.mazeWallsData,
                    state.mazeStartX, state.mazeStartY, state.mazeEndX, state.mazeEndY
            );

            // 1. Восстанавливаем базовую игру
            visualizer.restoreGameState(restoredMaze, state.robotX, state.robotY, state.robotDir, state.gameRunning);

            // 2. ИСПРАВЛЕНО: Передаем два аргумента (строку пикселей и сложность)
            visualizer.restoreFogPixels(state.fogPixelsData, state.difficulty);
        } else {
            visualizer.restoreGameState(null, state.robotX, state.robotY, state.robotDir, state.gameRunning);
        }
    }

    private static String extractArrayContent(String json, String key) {
        int start = json.indexOf("\"" + key + "\": [") + key.length() + 5;
        int end = json.indexOf("]", start);
        return json.substring(start, end);
    }

    public static boolean hasProgress() { return new File(PROGRESS_FILE).exists(); }
    public static void deleteProgress() { new File(PROGRESS_FILE).delete(); }
}
