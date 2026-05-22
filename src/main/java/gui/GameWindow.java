package gui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

public class GameWindow extends BaseInternalFrame {
    private final GameVisualizer m_visualizer;

    // Состояния нажатия клавиш для плавного диагонального движения
    private boolean up = false, down = false, left = false, right = false;

    public GameWindow() {
        super("Игровое поле", true, true, true, true);
        setDefaultCloseOperation(BaseInternalFrame.DO_NOTHING_ON_CLOSE);

        m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();

        // Слушатель клавиатуры
        m_visualizer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKey(e.getKeyCode(), false);
            }
        });

        // Запрашиваем фокус на игровое поле, чтобы клавиши считывались сразу
        m_visualizer.requestFocusInWindow();
        // Вызов ExitManager
        ExitManager.setupGameWindow(this);
    }

    private void handleKey(int keyCode, boolean isPressed) {
        // Проверяем WASD и Стрелочки
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) up = isPressed;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) down = isPressed;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) left = isPressed;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) right = isPressed;

        // Рассчитываем итоговый вектор направления
        int dx = 0;
        int dy = 0;

        if (left) dx -= 1;
        if (right) dx += 1;
        if (up) dy -= 1;
        if (down) dy += 1;

        // Передаем направление в визуализатор
        m_visualizer.setDirectionX(dx);
        m_visualizer.setDirectionY(dy);
    }


    public void generateNewMaze() {
        GameProgressManager.deleteProgress();
        Maze newMaze = new Maze(50, 30);
        m_visualizer.setMaze(newMaze);
    }
    public GameVisualizer getVisualizer() {
        return m_visualizer;
    }
}