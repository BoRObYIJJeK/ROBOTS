package gui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class GameProgressManagerTest {
    private GameVisualizer visualizer;
    private final int requestedWidth = 50;
    private final int requestedHeight = 30;

    @BeforeEach
    void setUp() {
        // Очищаем старые файлы перед каждым тестом
        GameProgressManager.deleteProgress();

        // Создаем визуализатор и инициализируем в нем тестовый лабиринт
        visualizer = new GameVisualizer();

        // ИСПРАВЛЕНО: Используем стандартный конструктор после отката кода
        Maze testMaze = new Maze(requestedWidth, requestedHeight);
        visualizer.setMaze(testMaze);
    }

    @AfterEach
    void tearDown() {
        // Убираем за собой тестовые файлы
        GameProgressManager.deleteProgress();
    }

    /**
     * 1. Проверяет, что при вызове saveProgress физически создается файл на диске
     */
    @Test
    void testSaveProgressCreatesFile() {
        assertFalse(GameProgressManager.hasProgress());

        GameProgressManager.saveProgress(visualizer);

        assertTrue(GameProgressManager.hasProgress());
        File file = new File("game_progress.json");
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    /**
     * 2. Проверяет точное сохранение и чтение координат и направления робота
     */
    @Test
    void testSaveAndLoadRobotCoordinates() {
        double testX = 245.1234;
        double testY = 180.5678;
        double testDir = 1.57;

        // Насильно двигаем робота в тестовую точку
        visualizer.restoreGameState(visualizer.getCurrentMaze(), testX, testY, testDir, true);

        // Сохраняем прогресс в JSON
        GameProgressManager.saveProgress(visualizer);

        // Считываем сохраненное состояние
        GameProgressManager.GameState loadedState = GameProgressManager.loadProgress();

        assertNotNull(loadedState, "Не удалось загрузить состояние игры");

        // Проверяем координаты с дельтой (погрешностью) из-за округления %.4f при записи в JSON
        assertEquals(testX, loadedState.robotX, 0.001, "Координата X робота повредилась при сохранении");
        assertEquals(testY, loadedState.robotY, 0.001, "Координата Y робота повредилась при сохранении");
        assertEquals(testDir, loadedState.robotDir, 0.001, "Направление робота повредилось при сохранении");
        assertTrue(loadedState.gameRunning);
    }

    /**
     * 3. Проверяет, что структура стен и размеры лабиринта восстанавливаются без искажений
     */
    @Test
    void testSaveAndLoadMazeStructure() {
        Maze originalMaze = visualizer.getCurrentMaze();
        assertNotNull(originalMaze);

        // Сохраняем
        GameProgressManager.saveProgress(visualizer);

        // Читаем
        GameProgressManager.GameState loadedState = GameProgressManager.loadProgress();
        assertNotNull(loadedState);

        // Проверяем метаданные лабиринта
        assertTrue(loadedState.hasMaze);
        assertEquals(originalMaze.getWidth(), loadedState.mazeWidth, "Ширина лабиринта изменилась");
        assertEquals(originalMaze.getHeight(), loadedState.mazeHeight, "Высота лабиринта изменилась");
        assertEquals(originalMaze.start.x, loadedState.mazeStartX);
        assertEquals(originalMaze.start.y, loadedState.mazeStartY);
        assertEquals(originalMaze.end.x, loadedState.mazeEndX);
        assertEquals(originalMaze.end.y, loadedState.mazeEndY);

        // ИСПРАВЛЕНО: Переписано на loadedState.mazeStartY, и восстановление идет через фабричный метод из Maze
        Maze restoredMaze = Maze.restoreFromProfile(
                loadedState.mazeWidth, loadedState.mazeHeight, loadedState.mazeWallsData,
                loadedState.mazeStartX, loadedState.mazeStartY, loadedState.mazeEndX, loadedState.mazeEndY
        );

        // Выборочно проверяем совпадение структуры стен во всем лабиринте
        for (int y = 0; y < originalMaze.getHeight(); y++) {
            for (int x = 0; x < originalMaze.getWidth(); x++) {
                assertEquals(originalMaze.isWall(x, y), restoredMaze.isWall(x, y),
                        String.format("Стена в ячейке (%d, %d) не совпадает после восстановления!", x, y));
            }
        }
    }

    /**
     * 4. Проверяет корректное удаление файла через метод deleteProgress()
     */
    @Test
    void testDeleteProgress() {
        GameProgressManager.saveProgress(visualizer);
        assertTrue(GameProgressManager.hasProgress());

        GameProgressManager.deleteProgress();

        assertFalse(GameProgressManager.hasProgress(), "Файл прогресса не удалился!");
    }
}
