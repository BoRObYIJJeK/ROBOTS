package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;
import java.awt.Point;

public class GameVisualizer extends JPanel {
    private final Timer m_timer = initTimer();
    private static Timer initTimer() {
        return new Timer("events generator", true);
    }

    private Maze currentMaze = null; // По умолчанию лабиринта НЕТ (равен null)
    private boolean gameRunning = true;

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;

    private volatile int moveX = 0;
    private volatile int moveY = 0;

    private static final double maxVelocity = 0.15;
    private static final int ROBOT_RADIUS = 8;

    public GameVisualizer() {
        // Устанавливаем дефолтный размер панели для пустого поля, пока лабиринт не загружен
        setPreferredSize(new Dimension(800, 600));
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() { onRedrawEvent(); }
        }, 0, 50);

        m_timer.schedule(new TimerTask() {
            @Override
            public void run() { onModelUpdateEvent(); }
        }, 0, 10);
        setDoubleBuffered(true);
        setFocusable(true);
    }

    // Геттеры для считывания состояния менеджером GameProgressManager
    public double getRobotX() { return m_robotPositionX; }
    public double getRobotY() { return m_robotPositionY; }
    public double getRobotDir() { return m_robotDirection; }
    public boolean isGameRunning() { return gameRunning; }
    public Maze getCurrentMaze() { return currentMaze; }

    public void setDirectionX(int dx) { this.moveX = dx; }
    public void setDirectionY(int dy) { this.moveY = dy; }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    protected void onModelUpdateEvent() {
        if (!gameRunning) return;
        if (moveX == 0 && moveY == 0) return;

        m_robotDirection = Math.atan2(moveY, moveX);

        if (currentMaze != null) {
            double duration = 10.0;
            double nextX = m_robotPositionX + maxVelocity * duration * moveX;
            double nextY = m_robotPositionY + maxVelocity * duration * moveY;

            if (canMoveTo(nextX, m_robotPositionY)) {
                m_robotPositionX = nextX;
            }
            if (canMoveTo(m_robotPositionX, nextY)) {
                m_robotPositionY = nextY;
            }

            Point currentCell = currentMaze.pixelToCell((int)m_robotPositionX, (int)m_robotPositionY);
            if (currentMaze.isExit(currentCell.x, currentCell.y)) {
                gameRunning = false;
                log.Logger.debug("Робот достиг финиша!");

                // Стираем файл прогресса, так как текущая игра успешно завершена
                GameProgressManager.deleteProgress();

                moveX = 0;
                moveY = 0;
                repaint();
            }
        } else {
            // Движение без лабиринта (в рамках обычного окна)
            double duration = 10.0;
            double newX = m_robotPositionX + maxVelocity * duration * Math.cos(m_robotDirection);
            double newY = m_robotPositionX + maxVelocity * duration * Math.sin(m_robotDirection);

            java.awt.Rectangle bounds = getBounds();
            if (bounds.width > 0 && bounds.height > 0) {
                newX = applyLimits(newX, ROBOT_RADIUS, bounds.width - ROBOT_RADIUS);
                newY = applyLimits(newY, ROBOT_RADIUS, bounds.height - ROBOT_RADIUS);
            }
            m_robotPositionX = newX;
            m_robotPositionY = newY;
        }
    }

    /**
     * Восстанавливает полное состояние игры из сохраненного прогресса
     */
    public void restoreGameState(Maze maze, double rx, double ry, double rdir, boolean running) {
        this.currentMaze = maze;
        this.m_robotPositionX = rx;
        this.m_robotPositionY = ry;
        this.m_robotDirection = rdir;
        this.gameRunning = running;

        if (maze != null) {
            setPreferredSize(new Dimension(maze.getTotalWidthPixels(), maze.getTotalHeightPixels()));
        } else {
            setPreferredSize(new Dimension(800, 600));
        }
        revalidate();
        repaint();
    }

    private boolean canMoveTo(double x, double y) {
        if (currentMaze == null) return true;
        return !isWallAt(x - ROBOT_RADIUS, y) && !isWallAt(x + ROBOT_RADIUS, y) && !isWallAt(x, y - ROBOT_RADIUS) && !isWallAt(x, y + ROBOT_RADIUS);
    }

    private boolean isWallAt(double pixelX, double pixelY) {
        if (currentMaze == null) return true;
        Point cell = currentMaze.pixelToCell((int) pixelX, (int) pixelY);
        if (cell.y < 0 || cell.y >= currentMaze.walls.length || cell.x < 0 || cell.x >= currentMaze.walls[cell.y].length) {
            return true;
        }
        return currentMaze.isWall(cell.x, cell.y);
    }

    private static double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static int round(double value) {
        return (int) (value + 0.5);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (currentMaze != null) {
            currentMaze.draw(g2d);
        }
        drawRobot(g2d, round(m_robotPositionX), round(m_robotPositionY), m_robotDirection);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction) {
        AffineTransform old = g.getTransform();
        g.rotate(direction, x, y);
        g.setColor(Color.MAGENTA);
        fillOval(g, x, y, 24, 8);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 24, 8);
        g.setColor(Color.WHITE);
        fillOval(g, x + 8, y, 4, 4);
        g.setColor(Color.BLACK);
        drawOval(g, x + 8, y, 4, 4);
        g.setTransform(old);
    }

    public void setMaze(Maze maze) {
        this.currentMaze = maze;
        this.gameRunning = true;
        setPreferredSize(new Dimension(maze.getTotalWidthPixels(), maze.getTotalHeightPixels()));
        revalidate();
        Point startPixel = maze.cellToPixel(maze.start.x, maze.start.y);
        m_robotPositionX = startPixel.x;
        m_robotPositionY = startPixel.y;
        m_robotDirection = 0;
        moveX = 0;
        moveY = 0;
        repaint();
    }
}
