package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

public class GameVisualizer extends JPanel {
    private final Timer m_timer = initTimer();
    private static Timer initTimer() {
        return new Timer("events generator", true);
    }

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;

    // Направление движения: -1 (влево/вверх), 0 (стоим), 1 (вправо/вниз)
    private volatile int moveX = 0;
    private volatile int moveY = 0;

    private static final double maxVelocity = 0.15; // Слегка увеличили скорость для динамики клавиатуры

    public GameVisualizer() {
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() { onRedrawEvent(); }
        }, 0, 50);

        m_timer.schedule(new TimerTask() {
            @Override
            public void run() { onModelUpdateEvent(); }
        }, 0, 10);

        setDoubleBuffered(true);
        setFocusable(true); // Чтобы компонент мог принимать фокус клавиатуры
    }

    // Эти методы будут вызываться из GameWindow при нажатии клавиш
    public void setDirectionX(int dx) { this.moveX = dx; }
    public void setDirectionY(int dy) { this.moveY = dy; }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    protected void onModelUpdateEvent() {
        // Если ни одна кнопка не зажата — робот просто стоит на месте
        if (moveX == 0 && moveY == 0) {
            return;
        }

        // Поворачиваем нос робота в сторону движения
        m_robotDirection = Math.atan2(moveY, moveX);

        // Движение вперед по вектору
        moveRobot(maxVelocity, 10);
    }

    private void moveRobot(double velocity, double duration) {
        double newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        double newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);

        // Ограничение движения границами экрана (чтобы робот не улетал за поле)
        int w = getWidth();
        int h = getHeight();
        int offset = 20;

        if (w > 0 && h > 0) {
            if (newX < offset) newX = offset;
            if (newX > w - offset) newX = w - offset;
            if (newY < offset) newY = offset;
            if (newY > h - offset) newY = h - offset;
        }

        m_robotPositionX = newX;
        m_robotPositionY = newY;
    }

    private static int round(double value) {
        return (int)(value + 0.5);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        drawRobot(g2d, m_robotDirection);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, double direction) {
        int x = round(m_robotPositionX);
        int y = round(m_robotPositionY);

        AffineTransform oldTransform = g.getTransform();
        AffineTransform t = AffineTransform.getRotateInstance(direction, x, y);
        g.setTransform(t);

        g.setColor(Color.MAGENTA);
        fillOval(g, x, y, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 30, 10);

        g.setColor(Color.WHITE);
        fillOval(g, x + 10, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x + 10, y, 5, 5);

        g.setTransform(oldTransform);
    }
}
