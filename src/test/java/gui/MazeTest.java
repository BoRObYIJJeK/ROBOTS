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
        maze = new Maze(requestedWidth, requestedHeight);
    }

    private void floodFill(int x, int y, boolean[][] visited) {
        if (x < 0 || y < 0 || x >= visited[0].length || y >= visited.length) return;
        if (maze.isWall(x, y) || visited[y][x]) return;
        visited[y][x] = true;
        floodFill(x - 1, y, visited);
        floodFill(x + 1, y, visited);
        floodFill(x, y - 1, visited);
        floodFill(x, y + 1, visited);
    }

    private int wallsAround(Point p) {
        int walls = 0;
        if (maze.isWall(p.x - 1, p.y)) walls++;
        if (maze.isWall(p.x + 1, p.y)) walls++;
        if (maze.isWall(p.x, p.y - 1)) walls++;
        if (maze.isWall(p.x, p.y + 1)) walls++;
        return walls;
    }

    private int passagesAround(Point p) {
        return 4 - wallsAround(p);
    }

    /** Старт — проход, не стена */
    @Test
    void testStartIsPassage() {
        Point start = maze.start;
        assertNotNull(start);
        assertEquals(1, start.x);
        assertEquals(1, start.y);
        assertFalse(maze.isWall(start.x, start.y));
    }

    /** Финиш — проход, isExit() работает */
    @Test
    void testExitIsPassage() {
        Point end = maze.end;
        assertNotNull(end);
        assertFalse(maze.isWall(end.x, end.y));
        assertTrue(maze.isExit(end.x, end.y));
    }

    /** Финиш — тупик (3+ стены вокруг) */
    @Test
    void testExitIsDeadEnd() {
        assertTrue(wallsAround(maze.end) >= 3);
    }

    /** К финишу ведёт ровно один путь */
    @Test
    void testExitHasOneEntrance() {
        assertEquals(1, passagesAround(maze.end));
    }

    /** Старт имеет хотя бы один выход */
    @Test
    void testStartHasExit() {
        assertTrue(passagesAround(maze.start) >= 1);
    }

    /** Из старта можно дойти до финиша */
    @Test
    void testCanReachExit() {
        boolean[][] visited = new boolean[maze.walls.length][maze.walls[0].length];
        floodFill(maze.start.x, maze.start.y, visited);
        assertTrue(visited[maze.end.y][maze.end.x]);
    }

    /** Все проходы достижимы из старта (связность) */
    @Test
    void testMazeIsConnected() {
        boolean[][] visited = new boolean[maze.walls.length][maze.walls[0].length];
        floodFill(maze.start.x, maze.start.y, visited);

        for (int y = 0; y < maze.walls.length; y++) {
            for (int x = 0; x < maze.walls[y].length; x++) {
                if (!maze.isWall(x, y)) {
                    assertTrue(visited[y][x]);
                }
            }
        }
    }

    /** Размеры лабиринта соответствуют запрошенным (с учётом нечётности) */
    @Test
    void testMazeDimensions() {
        int expectedW = requestedWidth % 2 == 0 ? requestedWidth + 1 : requestedWidth;
        int expectedH = requestedHeight % 2 == 0 ? requestedHeight + 1 : requestedHeight;
        assertEquals(expectedW, maze.getWidth());
        assertEquals(expectedH, maze.getHeight());
    }

    /** Внешние границы — сплошные стены */
    @Test
    void testOuterWallsClosed() {
        int w = maze.getWidth(), h = maze.getHeight();
        for (int x = 0; x < w; x++) {
            assertTrue(maze.isWall(x, 0));
            assertTrue(maze.isWall(x, h - 1));
        }
        for (int y = 0; y < h; y++) {
            assertTrue(maze.isWall(0, y));
            assertTrue(maze.isWall(w - 1, y));
        }
    }

    /** За границами лабиринта — стена */
    @Test
    void testOutOfBoundsIsWall() {
        assertTrue(maze.isWall(-1, 5));
        assertTrue(maze.isWall(5, -1));
        assertTrue(maze.isWall(1000, 5));
        assertTrue(maze.isWall(5, 1000));
    }

    /**
     * Проверяет попиксельную коллизию хитбокса робота со стенами.
     * Эмулирует логику canMoveTo из GameVisualizer, чтобы робот не заезжал в серые блоки.
     */
    @Test
    void testRobotCollisionWithWalls() {
        int cellSize = maze.getCellSize();
        int robotRadius = 8; // Наш стандартный радиус хитбокса для клеток 30px

        // Находим в лабиринте любую ячейку, которая гарантированно является стеной,
        // и соседнюю ячейку, которая является свободным проходом
        int wallCellX = -1;
        int passageCellX = -1;
        int passageCellY = -1;

        // Сканируем карту в поисках подходящей пары "проход-стена" по горизонтали
        for (int y = 1; y < maze.walls.length - 1; y++) {
            for (int x = 1; x < maze.walls[y].length - 2; x++) {
                if (!maze.isWall(x, y) && maze.isWall(x + 1, y)) {
                    passageCellX = x;
                    passageCellY = y;
                    wallCellX = x + 1;
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