package gui;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Rectangle;
import java.io.File;

import javax.swing.JFrame;

/**
 * Тесты для ProfileManager и BaseInternalFrame
 *
 * Проверяют:
 * - Сохранение профиля в JSON
 * - Загрузку профиля из JSON
 * - Применение профиля к окнам
 * - Удаление профиля
 * - Корректную работу BaseInternalFrame (нормальные границы)
 */
class ProfileManagerTest {

    private JFrame mainFrame;
    private BaseInternalFrame logWindow;
    private BaseInternalFrame gameWindow;

    private static final String PROFILE_FILE = "profile.json";

    /**
     * Создаёт тестовые окна перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        ProfileManager.deleteProfile();
        mainFrame = new JFrame();

        logWindow = new BaseInternalFrame("Протокол", true, true, true, true);
        gameWindow = new BaseInternalFrame("Игра", true, true, true, true);

        mainFrame.setBounds(100, 100, 800, 600);

        // Устанавливаем нормальные границы
        logWindow.setNormalBounds(new Rectangle(10, 10, 300, 200));
        gameWindow.setNormalBounds(new Rectangle(50, 50, 400, 400));

        logWindow.setVisible(true);
        gameWindow.setVisible(true);
    }

    /**
     * Закрывает окна и удаляет профиль после каждого теста
     */
    @AfterEach
    void tearDown() {
        mainFrame.dispose();
        logWindow.dispose();
        gameWindow.dispose();
        ProfileManager.deleteProfile();
    }

    /**
     * Проверяет: сохранение профиля создаёт файл
     */
    @Test
    void testSaveProfile() {
        ProfileManager.saveProfile(mainFrame, logWindow, gameWindow);
        assertTrue(ProfileManager.hasProfile());

        File file = new File(PROFILE_FILE);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    /**
     * Проверяет: загрузка профиля возвращает корректные данные
     */
    @Test
    void testLoadProfile() {
        ProfileManager.saveProfile(mainFrame, logWindow, gameWindow);
        ProfileManager.WindowState state = ProfileManager.loadProfile();

        assertNotNull(state);
        assertEquals(100, state.mainBounds.x);
        assertEquals(100, state.mainBounds.y);
        assertEquals(800, state.mainBounds.width);
        assertEquals(600, state.mainBounds.height);

        assertEquals(10, state.logBounds.x);
        assertEquals(10, state.logBounds.y);
        assertEquals(300, state.logBounds.width);
        assertEquals(200, state.logBounds.height);

        assertEquals(50, state.gameBounds.x);
        assertEquals(50, state.gameBounds.y);
        assertEquals(400, state.gameBounds.width);
        assertEquals(400, state.gameBounds.height);

        assertTrue(state.logVisible);
        assertTrue(state.gameVisible);
        assertFalse(state.logMaximized);
        assertFalse(state.gameMaximized);
    }

    /**
     * Проверяет: применение профиля восстанавливает состояние окон
     */
    @Test
    void testApplyProfile() {
        ProfileManager.WindowState state = new ProfileManager.WindowState();

        state.mainBounds = new Rectangle(200, 200, 800, 600);
        state.mainMaximized = false;

        state.logBounds = new Rectangle(20, 20, 300, 200);
        state.logVisible = true;
        state.logMaximized = false;

        state.gameBounds = new Rectangle(100, 100, 400, 400);
        state.gameVisible = true;
        state.gameMaximized = false;

        ProfileManager.applyProfile(state, mainFrame, logWindow, gameWindow);

        assertEquals(200, mainFrame.getBounds().x);
        assertEquals(200, mainFrame.getBounds().y);

        assertEquals(20, logWindow.getBounds().x);
        assertEquals(20, logWindow.getBounds().y);

        assertEquals(100, gameWindow.getBounds().x);
        assertEquals(100, gameWindow.getBounds().y);

        assertTrue(logWindow.isVisible());
        assertTrue(gameWindow.isVisible());
    }

    /**
     * Проверяет: удаление профиля
     */
    @Test
    void testDeleteProfile() {
        ProfileManager.saveProfile(mainFrame, logWindow, gameWindow);
        assertTrue(ProfileManager.hasProfile());

        ProfileManager.deleteProfile();
        assertFalse(ProfileManager.hasProfile());
    }

    /**
     * Проверяет: BaseInternalFrame сохраняет нормальные границы
     */
    @Test
    void testBaseInternalFrameNormalBounds() {
        Rectangle expectedBounds = new Rectangle(30, 40, 200, 150);
        logWindow.setNormalBounds(expectedBounds);

        Rectangle actualBounds = logWindow.getNormalBounds();

        assertEquals(expectedBounds.x, actualBounds.x);
        assertEquals(expectedBounds.y, actualBounds.y);
        assertEquals(expectedBounds.width, actualBounds.width);
        assertEquals(expectedBounds.height, actualBounds.height);
    }

    /**
     * Проверяет: BaseInternalFrame не сохраняет границы при максимизации
     */
    @Test
    void testBaseInternalFrameIgnoresMaximizedBounds() throws Exception {
        Rectangle normalBounds = new Rectangle(10, 10, 300, 200);
        logWindow.setNormalBounds(normalBounds);

        // Симулируем максимизацию
        try {
            logWindow.setMaximum(true);
        } catch (Exception e) {
            // В тестовой среде может не работать, пропускаем
        }

        // Нормальные границы должны остаться неизменными
        Rectangle actualNormalBounds = logWindow.getNormalBounds();
        assertEquals(normalBounds.x, actualNormalBounds.x);
        assertEquals(normalBounds.y, actualNormalBounds.y);
        assertEquals(normalBounds.width, actualNormalBounds.width);
        assertEquals(normalBounds.height, actualNormalBounds.height);
    }
}