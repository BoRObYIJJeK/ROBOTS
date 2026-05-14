package gui;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Класс для управления выходом из приложения и закрытием окон
 */
public class ExitManager {

    /**
     * Подтверждение выхода из приложения
     * @param frame главное окно
     * @param logWindow окно лога
     * @param gameWindow игровое окно
     */
    public static void confirmExit(JFrame frame, JInternalFrame logWindow, JInternalFrame gameWindow) {
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
        // ВОЗВРАЩАЕМ true ТОЛЬКО ЕСЛИ НАЖАЛИ "ДА"
        return result == JOptionPane.YES_OPTION;
    }
}