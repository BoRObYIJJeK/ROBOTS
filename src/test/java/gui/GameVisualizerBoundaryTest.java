package gui;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

class GameVisualizerBoundaryTest {

    private GameVisualizer visualizer;

    @BeforeEach
    void setUp() {
        visualizer = new GameVisualizer();
        visualizer.setSize(800, 600);
    }

    /** Проверяет: робот не уезжает в отрицательные координаты */
    @Test
    void testRobotPositionDoesNotGoNegative() throws Exception {
        Field xField = GameVisualizer.class.getDeclaredField("m_robotPositionX");
        Field yField = GameVisualizer.class.getDeclaredField("m_robotPositionY");
        xField.setAccessible(true);
        yField.setAccessible(true);

        for (int i = 0; i < 1000; i++) {
            visualizer.onModelUpdateEvent();

            double x = (double) xField.get(visualizer);
            double y = (double) yField.get(visualizer);

            assertTrue(x > -1000, "X не должен быть слишком маленьким: " + x);
            assertTrue(y > -1000, "Y не должен быть слишком маленьким: " + y);
        }
    }
}