package gui;

import java.awt.Rectangle;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import log.Logger;

public class ProfileManager {
    private static final String PROFILE_FILE = "profile.json";

    public static class WindowState {
        public Rectangle mainBounds;
        public boolean mainMaximized;
        public Rectangle logBounds;
        public boolean logVisible;
        public boolean logIcon;
        public boolean logMaximized;
        public Rectangle gameBounds;
        public boolean gameVisible;
        public boolean gameIcon;
        public boolean gameMaximized;
    }

    public static void saveProfile(JFrame mainFrame, BaseInternalFrame logWindow, BaseInternalFrame gameWindow) {
        WindowState s = new WindowState();
        s.mainBounds = mainFrame.getBounds();
        s.mainMaximized = (mainFrame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        s.logBounds = logWindow.getNormalBounds();
        s.logVisible = logWindow.isVisible();
        s.logMaximized = logWindow.isMaximum();
        s.logIcon = logWindow.isIcon();
        s.gameBounds = gameWindow.getNormalBounds();
        s.gameVisible = gameWindow.isVisible();
        s.gameMaximized = gameWindow.isMaximum();
        s.gameIcon = gameWindow.isIcon();

        String json = String.format(
                "{\n" +
                        "  \"main\": {\"x\":%d,\"y\":%d,\"w\":%d,\"h\":%d,\"max\":%b},\n" +
                        "  \"log\": {\"x\":%d,\"y\":%d,\"w\":%d,\"h\":%d,\"vis\":%b,\"max\":%b,\"icon\":%b},\n" +
                        "  \"game\": {\"x\":%d,\"y\":%d,\"w\":%d,\"h\":%d,\"vis\":%b,\"max\":%b,\"icon\":%b}\n" +
                        "}",
                s.mainBounds.x, s.mainBounds.y, s.mainBounds.width, s.mainBounds.height, s.mainMaximized,
                s.logBounds.x, s.logBounds.y, s.logBounds.width, s.logBounds.height, s.logVisible, s.logMaximized, s.logIcon,
                s.gameBounds.x, s.gameBounds.y, s.gameBounds.width, s.gameBounds.height, s.gameVisible, s.gameMaximized, s.gameIcon
        );

        try (FileWriter writer = new FileWriter(PROFILE_FILE)) {
            writer.write(json);
            Logger.debug("Профиль сохранён в JSON");
        } catch (IOException e) {
            Logger.error("Ошибка сохранения JSON: " + e.getMessage());
        }
    }

    public static WindowState loadProfile() {
        File file = new File(PROFILE_FILE);
        if (!file.exists()) return null;

        try {
            String content = new String(Files.readAllBytes(Paths.get(PROFILE_FILE)));
            WindowState state = new WindowState();

            state.mainBounds = parseRect(content, "main");
            state.mainMaximized = parseBool(content, "main", "max");

            state.logBounds = parseRect(content, "log");
            state.logVisible = parseBool(content, "log", "vis");
            state.logMaximized = parseBool(content, "log", "max");
            state.logIcon = parseBool(content, "log", "icon");

            state.gameBounds = parseRect(content, "game");
            state.gameVisible = parseBool(content, "game", "vis");
            state.gameMaximized = parseBool(content, "game", "max");
            state.gameIcon = parseBool(content, "game", "icon");

            return state;
        } catch (Exception e) {
            Logger.debug("Не удалось прочесть JSON профиль");
            return null;
        }
    }

    private static Rectangle parseRect(String json, String block) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + block + "\"\\s*:\\s*\\{\\s*\"x\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"y\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"w\"\\s*:\\s*(\\d+)\\s*,\\s*\"h\"\\s*:\\s*(\\d+)").matcher(json);
        if (m.find()) {
            return new Rectangle(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
        }
        return new Rectangle(0, 0, 400, 400);
    }

    private static boolean parseBool(String json, String block, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + block + "\"(.+?)\"" + key + "\"\\s*:\\s*(true|false)").matcher(json);
        return m.find() && Boolean.parseBoolean(m.group(2));
    }

    public static void applyProfile(WindowState state, JFrame mainFrame, BaseInternalFrame logWindow, BaseInternalFrame gameWindow) {
        if (state == null) return;
        if (state.mainMaximized) {
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        else if (state.mainBounds != null) {
            mainFrame.setBounds(state.mainBounds);
        }

        logWindow.setVisible(state.logVisible);
        if (state.logBounds != null && state.logBounds.width > 0) {
            logWindow.setNormalBounds(state.logBounds);
        }
        try {
            logWindow.setMaximum(false);
            logWindow.setIcon(false);

            if (state.logMaximized) {
                logWindow.setMaximum(true);
            }
            if (state.logIcon) {
                logWindow.setIcon(true);
            }
        }
        catch (Exception e) {}

        gameWindow.setVisible(state.gameVisible);
        if (state.gameBounds != null && state.gameBounds.width > 0) {
            gameWindow.setNormalBounds(state.gameBounds);
        }
        try {
            gameWindow.setMaximum(false);
            gameWindow.setIcon(false);

            if (state.gameMaximized) {
                gameWindow.setMaximum(true);
            }
            if (state.gameIcon) {
                gameWindow.setIcon(true);
            }
        }
        catch (Exception e) {}
    }

    public static boolean hasProfile() {
        return new File(PROFILE_FILE).exists();

    }
    public static boolean askRestoreProfile(JFrame parent) {
        return JOptionPane.showConfirmDialog(parent, "Восстановить окна?", "Профиль", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean askSaveProfile(JFrame parent) {
        return JOptionPane.showConfirmDialog(parent, "Сохранить профиль?", "Профиль", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void deleteProfile() {
        File file = new File(PROFILE_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
