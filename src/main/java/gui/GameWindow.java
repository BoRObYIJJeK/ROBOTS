package gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

// Наследуемся от нашего нового базового класса
public class GameWindow extends BaseInternalFrame {
    private final GameVisualizer m_visualizer;

    public GameWindow() {
        // Вызываем конструктор BaseInternalFrame с нужными параметрами
        super("Игровое поле", true, true, true, true);
        setDefaultCloseOperation(BaseInternalFrame.DO_NOTHING_ON_CLOSE);

        m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();

        // Вызов ExitManager
        ExitManager.setupGameWindow(this);
    }
}
