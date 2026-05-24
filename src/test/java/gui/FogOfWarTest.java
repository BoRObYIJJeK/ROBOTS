package gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FogOfWarTest {

    private FogOfWar fogOfWar;
    private final int mazeWidthCells = 10;
    private final int mazeHeightCells = 10;
    private final int cellSize = 30;

    @BeforeEach
    void setUp() {
        // Создаем свежий объект тумана перед каждым тестом (сетка 10х10 ячеек)
        fogOfWar = new FogOfWar(mazeWidthCells, mazeHeightCells, cellSize);
    }

    /**
     * Тест 1: Проверка базовой инициализации тумана.
     * Проверяет, что при старте игры вся карта полностью покрыта черным непрозрачным туманом ('1'),
     * и на холсте нет случайно открытых зон ('0').
     */
    @Test
    void testInitialFogIsCompletelyDark() {
        int testDifficulty = 1;
        String fogString = fogOfWar.getFogPixelsString(testDifficulty);

        assertNotNull(fogString, "Строка пикселей тумана не должна быть null");
        assertFalse(fogString.isEmpty(), "Строка пикселей тумана не должна быть пустой");
        assertFalse(fogString.contains("0"), "На старте лабиринт должен быть полностью скрыт ('1')");
    }

    /**
     * Тест 2: Проверка Нулевой сложности (Обычный лабиринт).
     * Проверяет, что на сложности 0 графическая маска тумана полностью игнорирует перемещения робота,
     * оставаясь неизменной (так как туман в этом режиме отключен).
     */
    @Test
    void testNormalDifficultyDoesNotChangeFog() {
        int normalDifficulty = 0;
        int staticDifficulty = 1;

        String beforeString = fogOfWar.getFogPixelsString(staticDifficulty);

        // Симулируем движение робота на сложности 0
        fogOfWar.updatePixels(100, 100, normalDifficulty);

        String afterString = fogOfWar.getFogPixelsString(staticDifficulty);
        assertEquals(beforeString, afterString, "На сложности 0 маска тумана не должна реагировать на робота");
    }

    /**
     * Тест 3: Проверка Первой сложности (Статический исследуемый туман).
     * Проверяет, что при перемещении робота в маске тумана успешно пробивается (стирается)
     * прозрачная область, кодируемая символами '0'.
     */
    @Test
    void testStaticFogRevealsOnMovement() {
        int difficulty = 1;

        // Робот делает шаг
        fogOfWar.updatePixels(100, 100, difficulty);
        String fogString = fogOfWar.getFogPixelsString(difficulty);

        assertTrue(fogString.contains("0"), "Туман должен пробиться (содержать '0') вокруг робота");
    }

    /**
     * Тест 4: Проверка Второй сложности (Динамический туман войны).
     * Проверяет ключевое свойство режима: старые позиции должны скрываться обратно в темноту.
     * Тест сверяет количество открытых пикселей ('0') и гарантирует, что видимость не накапливается,
     * а перемещается строго вслед за роботом.
     */
    @Test
    void testDynamicFogConcealsPreviousPositions() {
        int difficulty = 2; // Динамический туман

        // Шаг 1. Ставим робота в первую точку (80, 150) — строго по центру вертикали
        fogOfWar.updatePixels(80, 150, difficulty);
        String firstPositionFog = fogOfWar.getFogPixelsString(difficulty);
        assertTrue(firstPositionFog.contains("0"), "Динамический туман должен открывать область вокруг робота");

        long firstOpenCount = firstPositionFog.chars().filter(ch -> ch == '0').count();
        assertTrue(firstOpenCount > 0);

        // Шаг 2. Перемещаем робота строго горизонтально в точку (220, 150)
        // Высота осталась прежней, круг обзора гарантированно не упрется в верх или низ холста!
        fogOfWar.updatePixels(220, 150, difficulty);

        String secondPositionFog = fogOfWar.getFogPixelsString(difficulty);
        long secondOpenCount = secondPositionFog.chars().filter(ch -> ch == '0').count();

        // Главная проверка: старая зона полностью стерлась, новая открылась,
        // количество пикселей совпадает один в один, так как геометрия кругов идентична.
        assertEquals(firstOpenCount, secondOpenCount,
                "Динамический туман должен затягивать старые позиции, оставляя видимой только зону вокруг робота");
    }


    /**
     * Тест 5: Проверка попиксельного восстановления текстуры.
     * Проверяет, что метод загрузки маски восстанавливает картинку со 100% точностью.
     * Измененная маска кодируется в битовую строку, заливается в совершенно новый объект тумана
     * и проверяется символ в символ.
     */
    @Test
    void testFogSaveAndRestoreMatchExactly() {
        int difficulty = 1;

        // Симулируем движение, чтобы протереть уникальный узор дорожек
        fogOfWar.updatePixels(45, 45, difficulty);
        String originalFogString = fogOfWar.getFogPixelsString(difficulty);

        // Имитируем перезапуск игры: создаем новый пустой объект тумана
        FogOfWar restoredFog = new FogOfWar(mazeWidthCells, mazeHeightCells, cellSize);

        // Накатываем на него сохраненную битовую строку пикселей
        restoredFog.restoreFromPixelsString(originalFogString, difficulty);

        String fullyRestoredString = restoredFog.getFogPixelsString(difficulty);
        assertEquals(originalFogString, fullyRestoredString, "Восстановленная попиксельная маска отличается от исходной");
    }
}
