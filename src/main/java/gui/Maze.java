package gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Maze {
    private int width;
    private int height;
    private final int cellSize = 30; // Читаемый крупный масштаб
    public boolean[][] walls;
    public Point start;
    public Point end;
    private static final Random random = new Random();

    public Maze(int width, int height) {
        // Первое число — строго горизонталь (X), второе — вертикаль (Y)
        int finalWidth = Math.max(width, height);
        int finalHeight = Math.min(width, height);

        this.width = finalWidth % 2 == 0 ? finalWidth + 1 : finalWidth;
        this.height = finalHeight % 2 == 0 ? finalHeight + 1 : finalHeight;
        generateMaze();
    }

    private void generateMaze() {
        walls = new boolean[this.height][this.width];
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                walls[y][x] = true;
            }
        }

        List<Point> visited = new ArrayList<>();
        Point startCell = new Point(1, 1);
        walls[startCell.y][startCell.x] = false;
        visited.add(startCell);

        List<Point[]> frontiers = new ArrayList<>();
        addFrontiers(startCell, frontiers);

        Point furthestCell = startCell;
        int maxDistance = 0;

        while (!frontiers.isEmpty()) {
            int index = random.nextInt(frontiers.size());
            Point[] edge = frontiers.remove(index);

            Point wall = edge[0];
            Point neighbor = edge[1];

            if (walls[neighbor.y][neighbor.x]) {
                walls[wall.y][wall.x] = false;
                walls[neighbor.y][neighbor.x] = false;

                visited.add(neighbor);
                addFrontiers(neighbor, frontiers);

                // Ищем самый глубокий тупик лабиринта
                int dist = Math.abs(neighbor.x - startCell.x) + Math.abs(neighbor.y - startCell.y);
                if (dist > maxDistance && isDeadEnd(neighbor)) {
                    maxDistance = dist;
                    furthestCell = neighbor;
                }
            }
        }

        start = startCell;
        end = furthestCell;

        // Искусственно изолируем финиш, делая его строгим тупиком с 1 входом
        ensureStrictDeadEnd(end);
    }

    /**
     * Сканирует стены вокруг зелёной клетки и замуровывает лишние проходы.
     * Гарантирует, что к финишу ведёт строго ОДИН путь.
     */
    private void ensureStrictDeadEnd(Point target) {
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int openPassages = 0;

        for (int[] dir : dirs) {
            int nx = target.x + dir[0];
            int ny = target.y + dir[1];
            if (nx >= 0 && nx < width && ny >= 0 && ny < height && !walls[ny][nx]) {
                openPassages++;
            }
        }

        // Если алгоритм случайно прорубил к этой клетке больше одного коридора, застраиваем лишние
        if (openPassages > 1) {
            int built = 0;
            for (int[] dir : dirs) {
                int nx = target.x + dir[0];
                int ny = target.y + dir[1];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height && !walls[ny][nx]) {
                    if (built < openPassages - 1) {
                        walls[ny][nx] = true; // Ставим стену обратно
                        built++;
                    }
                }
            }
        }
    }

    private void addFrontiers(Point p, List<Point[]> frontiers) {
        int[][] dirs = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
        for (int[] dir : dirs) {
            int nx = p.x + dir[0];
            int ny = p.y + dir[1];
            int wx = p.x + dir[0] / 2;
            int wy = p.y + dir[1] / 2;

            if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1) {
                if (walls[ny][nx]) {
                    frontiers.add(new Point[]{new Point(wx, wy), new Point(nx, ny)});
                }
            }
        }
    }

    private boolean isDeadEnd(Point p) {
        int wallsCount = 0;
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] dir : dirs) {
            if (walls[p.y + dir[1]][p.x + dir[0]]) {
                wallsCount++;
            }
        }
        return wallsCount >= 3;
    }

    public boolean isWall(int cellX, int cellY) {
        if (cellX < 0 || cellY < 0 || cellX >= width || cellY >= height) {
            return true;
        }
        return walls[cellY][cellX];
    }

    public boolean isExit(int cellX, int cellY) {
        return cellX == end.x && cellY == end.y;
    }

    public Point pixelToCell(int pixelX, int pixelY) {
        return new Point(pixelX / cellSize, pixelY / cellSize);
    }

    public Point cellToPixel(int cellX, int cellY) {
        return new Point(cellX * cellSize + cellSize / 2, cellY * cellSize + cellSize / 2);
    }

    public int getCellSize() {
        return cellSize;
    }

    public int getTotalWidthPixels() { return width * cellSize; }
    public int getTotalHeightPixels() { return height * cellSize; }

    public void draw(Graphics2D g) {
        // Стены
        g.setColor(new Color(60, 60, 60));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (walls[y][x]) {
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }

        // Подсвечиваем сложный тупиковый финиш зелёным цветом
        if (end != null) {
            g.setColor(Color.GREEN);
            g.fillRect(end.x * cellSize, end.y * cellSize, cellSize, cellSize);
        }

        // Сетка
        g.setColor(new Color(80, 80, 80));
        for (int y = 0; y <= height; y++) {
            g.drawLine(0, y * cellSize, width * cellSize, y * cellSize);
        }
        for (int x = 0; x <= width; x++) {
            g.drawLine(x * cellSize, 0, x * cellSize, height * cellSize);
        }
    }
}
