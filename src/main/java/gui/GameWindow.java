package gui;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;

    public GameWindow()
    {
        super("Игровое поле", true, true, true, true);

        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();

        // Заменяем полную логику на вызов ExitManager
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (ExitManager.confirmWindowClose(GameWindow.this, "игровое окно")) {
                    dispose();
                }
            }
        });
    }
}