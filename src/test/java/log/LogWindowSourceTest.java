package log;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * - Ограничение размера очереди (не более N сообщений)
 * - Регистрацию и удаление слушателей (предотвращение утечек памяти)
 */
class LogWindowSourceTest {

    private LogWindowSource logSource;
    private TestLogListener listener;

    /**
     * Настройка перед каждым тестом.
     * Создаёт источник логов с очередью на 5 сообщений и регистрирует тестового слушателя.
     */
    @BeforeEach
    void setUp() {
        logSource = new LogWindowSource(5); // очередь на 5 сообщений
        listener = new TestLogListener();
        logSource.registerListener(listener);
    }

    /**
     * Очистка после каждого теста.
     * Удаляет тестового слушателя, чтобы не было утечек памяти.
     */
    @AfterEach
    void tearDown() {
        logSource.unregisterListener(listener);
    }

    /**
     * Тест: ограничение размера очереди.
     * Добавляет 7 сообщений при лимите 5.
     * Проверяет, что в логе осталось только 5 последних сообщений,
     * а первые два были удалены.
     */
    @Test
    void testQueueLimit() {
        // Добавляем 7 сообщений при лимите 5
        for (int i = 0; i < 7; i++) {
            logSource.append(LogLevel.Debug, "Message " + i);
        }

        assertEquals(5, logSource.size());

        Iterable<LogEntry> all = logSource.all();
        List<LogEntry> list = new ArrayList<>();
        all.forEach(list::add);

        // Первые два сообщения (0 и 1) должны быть удалены
        assertEquals("Message 2", list.get(0).getMessage());
        assertEquals("Message 3", list.get(1).getMessage());
        assertEquals("Message 4", list.get(2).getMessage());
        assertEquals("Message 5", list.get(3).getMessage());
        assertEquals("Message 6", list.get(4).getMessage());
    }

    /**
     * Тест: регистрация и удаление слушателей.
     * Проверяет, что слушатели получают уведомления,
     * а после удаления - не получают.
     * Важно для предотвращения утечек памяти.
     */
    @Test
    void testRegisterAndUnregisterListener() {
        TestLogListener secondListener = new TestLogListener();
        logSource.registerListener(secondListener);

        // Добавляем сообщение - оба слушателя должны получить уведомление
        logSource.append(LogLevel.Debug, "Test");

        assertTrue(listener.isNotified());
        assertTrue(secondListener.isNotified());

        // Сбрасываем флаги
        listener.reset();
        secondListener.reset();

        // Удаляем второго слушателя
        logSource.unregisterListener(secondListener);

        // Добавляем ещё одно сообщение
        logSource.append(LogLevel.Debug, "Second test");

        // Первый слушатель должен получить уведомление
        assertTrue(listener.isNotified());
        // Второй слушатель НЕ должен получить уведомление (отписан)
        assertFalse(secondListener.isNotified());
    }

    /**
     * Вспомогательный слушатель для тестов.
     * Просто запоминает, было ли уведомление.
     */
    static class TestLogListener implements LogChangeListener {
        private boolean notified = false;

        @Override
        public void onLogChanged() {
            notified = true;
        }

        public boolean isNotified() {
            return notified;
        }

        public void reset() {
            notified = false;
        }
    }
}