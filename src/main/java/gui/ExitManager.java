package gui;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import log.LogWindowSource;

/**
 * Класс для управления выходом из приложения и закрытием окон
 */
public class ExitManager {

    /**
     * Подтверждение выхода из приложения
     * @param frame главное окно
     * @param logWindow окно лога (изменено на BaseInternalFrame)
     * @param gameWindow игровое окно (изменено на BaseInternalFrame)
     */
    public static void confirmExit(JFrame frame, BaseInternalFrame logWindow, BaseInternalFrame gameWindow) {
        int result = JOptionPane.showConfirmDialog(
                frame,
                "Выйти из приложения?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            if (ProfileManager.askSaveProfile(frame)) {
                ProfileManager.saveProfile(frame, logWindow, gameWindow);
            }
            System.exit(0);
        }
    }

    /**
     * Подтверждение закрытия внутреннего окна
     * @param window окно для закрытия
     * @param title название окна
     * @return true только если пользователь нажал ДА
     */
    public static boolean confirmWindowClose(JInternalFrame window, String title) {
        int result = JOptionPane.showConfirmDialog(
                window,
                "Закрыть " + title + "?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Настройка игрового окна (подтверждение закрытия)
     * @param gameWindow игровое окно
     */
    public static void setupGameWindow(GameWindow gameWindow) {
        gameWindow.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        gameWindow.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (confirmWindowClose(gameWindow, "игровое окно")) {
                    gameWindow.dispose();
                }
            }
        });
    }

    /**
     * Настройка окна лога (подтверждение закрытия)
     * @param logWindow окно лога
     * @param logSource источник логов (для отписки)
     */
    public static void setupLogWindow(LogWindow logWindow, LogWindowSource logSource) {
        logWindow.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        logWindow.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (confirmWindowClose(logWindow, "окно лога")) {
                    logSource.unregisterListener(logWindow);
                    logWindow.dispose();
                }
            }
        });
    }
}
