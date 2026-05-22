package gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;

class MazeTest {
    private Maze maze;
    private final int requestedWidth = 50;
    private final int requestedHeight = 30;

    @BeforeEach
    void setUp() {
        // Создаем лабиринт в наших стандартных пропорциях перед каждым тестом
        maze = new Maze(requestedWidth, requestedHeight);
    }
    /**
     * 1. Проверяет, что точка старта (1,1) всегда является проходом, а не стеной
     */
    @Test
    void testStartIsPassage() {
        Point start = maze.start;
        assertNotNull(start, "Точка старта не должна быть null");
        assertEquals(1, start.x);
        assertEquals(1, start.y);
        assertFalse(maze.isWall(start.x, start.y), "Стартовая ячейка заблокирована стеной!");
    }

    /**
     * 2. Проверяет, что зеленая клетка финиша всегда является проходом
     */
    @Test
    void testExitIsPassage() {
        Point end = maze.end;
        assertNotNull(end, "Точка финиша не должна быть null");
        assertFalse(maze.isWall(end.x, end.y), "Финишная ячейка заблокирована стеной!");
        assertTrue(maze.isExit(end.x, end.y), "Метод isExit не распознает финишную клетку");
    }

    /**
     * 3. Проверяет корректность математической конвертации пикселей в клетки и обратно
     */
    @Test
    void testCoordinateConversion() {
        int cellSize = maze.getCellSize();

        // Тестируем клетку (3, 5)
        int cellX = 3;
        int cellY = 5;

        // Клетка -> Пиксели (центр ячейки)
        Point pixel = maze.cellToPixel(cellX, cellY);
        int expectedPixelX = cellX * cellSize + cellSize / 2;
        int expectedPixelY = cellY * cellSize + cellSize / 2;
        assertEquals(expectedPixelX, pixel.x);
        assertEquals(expectedPixelY, pixel.y);

        // Пиксели -> Клетка
        Point convertedCell = maze.pixelToCell(pixel.x, pixel.y);
        assertEquals(cellX, convertedCell.x);
        assertEquals(cellY, convertedCell.y);
    }

    /**
     * 4. Проверяет защитную логику: координаты за пределами лабиринта должны считаться стеной
     */
    @Test
    void testOutOfBoundsIsWall() {
        // Тестируем отрицательные индексы
        assertTrue(maze.isWall(-1, 5));
        assertTrue(maze.isWall(5, -1));

        // Тестируем индексы, выходящие далеко за пределы массива
        assertTrue(maze.isWall(100, 5));
        assertTrue(maze.isWall(5, 100));
    }
    /**
     * 5. Проверяет попиксельную коллизию хитбокса робота со стенами.
     * Эмулирует логику canMoveTo из GameVisualizer, чтобы робот не заезжал в серые блоки.
     */
    @Test
    void testRobotCollisionWithWalls() {
        int cellSize = maze.getCellSize();
        int robotRadius = 8; // Наш стандартный радиус хитбокса для клеток 30px

        // Находим в лабиринте любую ячейку, которая гарантированно является стеной,
        // и соседнюю ячейку, которая является свободным проходом.
        int wallCellX = -1;
        int wallCellY = -1;
        int passageCellX = -1;
        int passageCellY = -1;

        // Сканируем карту в поисках подходящей пары "проход-стена" по горизонтали
        for (int y = 1; y < maze.walls.length - 1; y++) {
            for (int x = 1; x < maze.walls[y].length - 2; x++) {
                if (!maze.isWall(x, y) && maze.isWall(x + 1, y)) {
                    passageCellX = x;
                    passageCellY = y;
                    wallCellX = x + 1;
                    wallCellY = y;
                    break;
                }
            }
            if (wallCellX != -1) break;
        }

        // Убеждаемся, что тестовые ячейки успешно найдены
        assertNotNull(maze.walls);
        assertTrue(passageCellX > 0, "Не удалось найти проход для теста");
        assertTrue(wallCellX > 0, "Не удалось найти стену для теста");

        // Получаем точный пиксельный центр свободной ячейки (безопасная зона)
        Point passagePixel = maze.cellToPixel(passageCellX, passageCellY);

        // Проверяем 4 крайние точки робота, когда он стоит идеально по центру прохода.
        // Переводим пиксели хитбокса в ячейки лабиринта.
        Point leftEdge = maze.pixelToCell(passagePixel.x - robotRadius, passagePixel.y);
        Point rightEdge = maze.pixelToCell(passagePixel.x + robotRadius, passagePixel.y);
        Point topEdge = maze.pixelToCell(passagePixel.x, passagePixel.y - robotRadius);
        Point bottomEdge = maze.pixelToCell(passagePixel.x, passagePixel.y + robotRadius);

        // В центре прохода ни одна крайняя точка робота не должна задевать стену
        assertFalse(maze.isWall(leftEdge.x, leftEdge.y), "Хитбокс робота задел левую стену в центре прохода");
        assertFalse(maze.isWall(rightEdge.x, rightEdge.y), "Хитбокс robot задел правую стену в центре прохода");
        assertFalse(maze.isWall(topEdge.x, topEdge.y), "Хитбокс робота задел верхнюю стену в центре прохода");
        assertFalse(maze.isWall(bottomEdge.x, bottomEdge.y), "Хитбокс робота задел нижнюю стену в центре прохода");

        // --- ИМИТАЦИЯ КРИТИЧЕСКОГО СДВИГА К СТЕНЕ ---
        // Сдвигаем робота вправо к стене (wallCellX) на расстояние, при котором
        // его правый край физически заезжает на первый пиксель текстуры стены.
        int unsafePixelX = wallCellX * cellSize - 1; // Точка прямо перед стеной
        int robotCollidingX = unsafePixelX - robotRadius + 1; // Сдвигаем центр так, чтобы край пересек черту

        // Проверяем правую крайнюю точку хитбокса в этой смещенной позиции
        Point collidingRightEdge = maze.pixelToCell(robotCollidingX + robotRadius, passagePixel.y);

        // Тест должен подтвердить коллизию: игра обязана увидеть стену и заблокировать шаг
        assertTrue(maze.isWall(collidingRightEdge.x, collidingRightEdge.y),
                "Система коллизий пропустила хитбокс робота внутрь текстуры стены!");
    }

}