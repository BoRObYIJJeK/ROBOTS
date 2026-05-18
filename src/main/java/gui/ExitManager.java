package gui;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import log.LogWindowSource;

public class ExitManager {

    public static void confirmExit(JFrame frame, BaseInternalFrame logWindow, BaseInternalFrame gameWindow) {
        if (confirmAction(frame, "Выйти из приложения?")) {
            if (ProfileManager.askSaveProfile(frame)) {
                ProfileManager.saveProfile(frame, logWindow, gameWindow);
            }
            System.exit(0);
        }
    }

    public static boolean confirmWindowClose(JInternalFrame window, String title) {
        return confirmAction(window, "Закрыть " + title + "?");
    }

    public static void setupGameWindow(GameWindow gameWindow) {
        setupWindow(gameWindow, "игровое окно", () -> {});
    }

    public static void setupLogWindow(LogWindow logWindow, LogWindowSource logSource) {
        setupWindow(logWindow, "окно лога", () -> logSource.unregisterListener(logWindow));
    }

    // Универсальный метод настройки окон (убирает дублирование слушателей)
    private static void setupWindow(JInternalFrame window, String title, Runnable onBeforeClose) {
        window.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        window.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (confirmWindowClose(window, title)) {
                    onBeforeClose.run();
                    window.dispose();
                }
            }
        });
    }

    // Единый метод вызова JOptionPane (убирает дублирование диалогов)
    private static boolean confirmAction(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Подтверждение", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
