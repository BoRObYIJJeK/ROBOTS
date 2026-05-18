package gui;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Rectangle;
import java.io.File;
import javax.swing.JFrame;

class ProfileManagerTest {
    private JFrame mainFrame;
    private BaseInternalFrame logWindow;  // Изменено с JInternalFrame на BaseInternalFrame
    private BaseInternalFrame gameWindow; // Изменено с JInternalFrame на BaseInternalFrame

    /**
     * Создаёт тестовые окна перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        ProfileManager.deleteProfile(); // удаляем старый профиль
        mainFrame = new JFrame();

        // Создаем экземпляры нашего нового класса с дефолтными параметрами
        logWindow = new BaseInternalFrame("Протокол", true, true, true, true);
        gameWindow = new BaseInternalFrame("Игра", true, true, true, true);

        mainFrame.setBounds(100, 100, 800, 600);

        // Важно: для BaseInternalFrame используем setNormalBounds, чтобы инициализировать внутреннее состояние
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
        File file = new File("profile.ser");
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
        assertEquals(10, state.logBounds.x);
        assertEquals(50, state.gameBounds.x);
        assertTrue(state.logVisible);
        assertTrue(state.gameVisible);
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
        state.gameBounds = new Rectangle(100, 100, 400, 400);
        state.gameVisible = true;

        ProfileManager.applyProfile(state, mainFrame, logWindow, gameWindow);
        assertEquals(200, mainFrame.getBounds().x);
        assertEquals(20, logWindow.getBounds().x);
        assertEquals(100, gameWindow.getBounds().x);
    }

    /**
     * Проверяет: удаление профиля
     */
    @Test
    void testDeleteProfile() {
        ProfileManager.saveProfile(mainFrame, logWindow, gameWindow);
        assertTrue(ProfileManager.hasProfile());
        ProfileManager.deleteProfile();
        assertFalse(ProfileManager.hasProfile()); // Исправлено здесь
    }
}
