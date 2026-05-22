package gui;

import java.awt.Rectangle;
import java.io.File;
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

    public static void saveProfile(JFrame main, BaseInternalFrame log, BaseInternalFrame game) {
        try {
            WindowState s = new WindowState();
            s.mainBounds = main.getBounds();
            s.mainMaximized = (main.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;

            s.logBounds = log.getNormalBounds();
            s.logVisible = log.isVisible();
            s.logMaximized = log.isMaximum();
            s.logIcon = log.isIcon();

            s.gameBounds = game.getNormalBounds();
            s.gameVisible = game.isVisible();
            s.gameMaximized = game.isMaximum();
            s.gameIcon = game.isIcon();

            // Формируем красивый и компактный JSON массив настроек окон
            String json = String.format(
                    "{\n" +
                            "  \"main\": [ %d, %d, %d, %d, %b ],\n" +
                            "  \"log\":  [ %d, %d, %d, %d, %b, %b, %b ],\n" +
                            "  \"game\": [ %d, %d, %d, %d, %b, %b, %b ]\n" +
                            "}",
                    s.mainBounds.x, s.mainBounds.y, s.mainBounds.width, s.mainBounds.height, s.mainMaximized,
                    s.logBounds.x, s.logBounds.y, s.logBounds.width, s.logBounds.height, s.logVisible, s.logMaximized, s.logIcon,
                    s.gameBounds.x, s.gameBounds.y, s.gameBounds.width, s.gameBounds.height, s.gameVisible, s.gameMaximized, s.gameIcon
            );

            Files.write(Paths.get(PROFILE_FILE), json.getBytes());
            Logger.debug("Профиль окон успешно сохранен в JSON");
        } catch (Exception e) {
            Logger.error("Ошибка сохранения профиля окон: " + e.getMessage());
        }
    }

    public static WindowState loadProfile() {
        if (!hasProfile()) return null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(PROFILE_FILE)));
            WindowState state = new WindowState();

            state.mainBounds = parseRect(content, "main");
            state.mainMaximized = parseBool(content, "main", 4);

            state.logBounds = parseRect(content, "log");
            state.logVisible = parseBool(content, "log", 4);
            state.logMaximized = parseBool(content, "log", 5);
            state.logIcon = parseBool(content, "log", 6);

            state.gameBounds = parseRect(content, "game");
            state.gameVisible = parseBool(content, "game", 4);
            state.gameMaximized = parseBool(content, "game", 5);
            state.gameIcon = parseBool(content, "game", 6);

            return state;
        } catch (Exception e) {
            Logger.error("Ошибка загрузки профиля окон: " + e.getMessage());
            return null;
        }
    }

    public static void applyProfile(WindowState state, JFrame main, BaseInternalFrame log, BaseInternalFrame game) {
        if (state == null) return;

        // 1. Восстановление главного окна
        if (state.mainMaximized) main.setExtendedState(JFrame.MAXIMIZED_BOTH);
        else if (state.mainBounds != null) main.setBounds(state.mainBounds);

        // 2. Восстановление окна логов
        log.setVisible(state.logVisible);
        if (state.logBounds != null) log.setNormalBounds(state.logBounds);
        restoreFrameState(log, state.logMaximized, state.logIcon);

        // 3. Восстановление окна игры
        game.setVisible(state.gameVisible);
        if (state.gameBounds != null) game.setNormalBounds(state.gameBounds);
        restoreFrameState(game, state.gameMaximized, state.gameIcon);

        Logger.debug("Профиль успешно восстановлен");
    }

    private static void restoreFrameState(BaseInternalFrame frame, boolean isMax, boolean isIcon) {
        try {
            frame.setMaximum(false);
            frame.setIcon(false);
            if (isMax) frame.setMaximum(true);
            if (isIcon) frame.setIcon(true);
        } catch (Exception e) {}
    }

    // ИСПРАВЛЕНО: Быстрый и безопасный разбор прямоугольника по запятым без регулярных выражений
    private static Rectangle parseRect(String json, String key) {
        try {
            String arrayContent = extractArrayContent(json, key);
            String[] tokens = arrayContent.split(",");
            return new Rectangle(
                    Integer.parseInt(tokens[0].trim()), Integer.parseInt(tokens[1].trim()),
                    Integer.parseInt(tokens[2].trim()), Integer.parseInt(tokens[3].trim())
            );
        } catch (Exception e) {
            return new Rectangle(10, 10, 400, 400);
        }
    }

    // ИСПРАВЛЕНО: Быстрый разбор логических флагов по их индексам
    private static boolean parseBool(String json, String key, int index) {
        try {
            String arrayContent = extractArrayContent(json, key);
            String[] tokens = arrayContent.split(",");
            return Boolean.parseBoolean(tokens[index].trim());
        } catch (Exception e) {
            return false;
        }
    }

    // ИСПРАВЛЕНО: Надежный хелпер для вырезания содержимого внутри квадратных скобок [...]
    private static String extractArrayContent(String json, String key) {
        int start = json.indexOf("\"" + key + "\": [") + key.length() + 5;
        int end = json.indexOf("]", start);
        return json.substring(start, end);
    }

    public static boolean hasProfile() { return new File(PROFILE_FILE).exists(); }
    public static void deleteProfile() { new File(PROFILE_FILE).delete(); }

    // УНИВЕРСАЛЬНЫЕ МЕТОДЫ: Принимают java.awt.Component для совместимости со всеми окнами Swing
    public static boolean askSaveProfile(java.awt.Component p) {
        return JOptionPane.showConfirmDialog(p, "Сохранить профиль?", "Сохранение настроек", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean askRestoreProfile(java.awt.Component p) {
        return JOptionPane.showConfirmDialog(p, "Восстановить профиль?", "Восстановление настроек", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
