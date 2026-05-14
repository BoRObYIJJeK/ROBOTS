package gui;

import java.io.*;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import log.Logger;

public class ProfileManager {

    private static final String PROFILE_FILE = "profile.ser";

    // Класс для хранения состояния окон
    public static class WindowState implements Serializable {
        private static final long serialVersionUID = 1L;

        public Rectangle mainBounds;
        public boolean mainMaximized;
        public Rectangle logBounds;
        public boolean logVisible;
        public boolean logIcon;
        public Rectangle gameBounds;
        public boolean gameVisible;
        public boolean gameIcon;
    }

    public static void saveProfile(JFrame mainFrame, JInternalFrame logWindow, JInternalFrame gameWindow) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROFILE_FILE))) {

            WindowState state = new WindowState();

            state.mainBounds = mainFrame.getBounds();
            state.mainMaximized = (mainFrame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;

            state.logBounds = logWindow.getBounds();
            state.logVisible = logWindow.isVisible();
            state.logIcon = logWindow.isIcon();

            state.gameBounds = gameWindow.getBounds();
            state.gameVisible = gameWindow.isVisible();
            state.gameIcon = gameWindow.isIcon();

            oos.writeObject(state);
            Logger.debug("Профиль сохранён");

        } catch (IOException e) {
            Logger.error("Ошибка сохранения профиля: " + e.getMessage());
        }
    }

    public static WindowState loadProfile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PROFILE_FILE))) {
            return (WindowState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Logger.debug("Сохранённый профиль не найден");
            return null;
        }
    }

    public static void applyProfile(WindowState state, JFrame mainFrame,
                                    JInternalFrame logWindow, JInternalFrame gameWindow) {
        if (state == null) return;

        if (state.mainMaximized) {
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            mainFrame.setBounds(state.mainBounds);
        }

        logWindow.setBounds(state.logBounds);
        logWindow.setVisible(state.logVisible);
        try {
            if (state.logIcon) logWindow.setIcon(true);
        } catch (Exception e) {}

        gameWindow.setBounds(state.gameBounds);
        gameWindow.setVisible(state.gameVisible);
        try {
            if (state.gameIcon) gameWindow.setIcon(true);
        } catch (Exception e) {}

        Logger.debug("Профиль восстановлен");
    }

    public static boolean hasProfile() {
        File file = new File(PROFILE_FILE);
        return file.exists();
    }

    public static boolean askRestoreProfile(JFrame parent) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                "Обнаружен сохранённый профиль. Восстановить состояние окон?",
                "Восстановление профиля",
                JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }

    public static boolean askSaveProfile(JFrame parent) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                "Сохранить состояние окон перед выходом?",
                "Сохранение профиля",
                JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }

    public static void deleteProfile() {
        File file = new File(PROFILE_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}