package gui;

import javax.swing.JInternalFrame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class BaseInternalFrame extends JInternalFrame {
    // Храним чистые, нормальные размеры окна
    private Rectangle normalBounds;

    public BaseInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);

        // Слушаем только реальные передвижения и изменения размера пользователем
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                savePureBounds();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                savePureBounds();
            }
        });
    }

    private void savePureBounds() {
        // КРИТИЧЕСКИЙ МОМЕНТ: если окно свернуто, развернуто или невидимка,
        // Swing ломает bounds. В эти моменты сохранять координаты НЕЛЬЗЯ.
        if (!isMaximum() && !isIcon() && isShowing() && getWidth() > 0 && getHeight() > 0) {
            this.normalBounds = getBounds();
        }
    }

    public Rectangle getNormalBounds() {
        // Если пользователь ни разу не двигал окно, берем его текущие bounds
        if (normalBounds == null || normalBounds.width <= 0 || normalBounds.height <= 0) {
            return getBounds();
        }
        return normalBounds;
    }

    public void setNormalBounds(Rectangle bounds) {
        if (bounds != null && bounds.width > 0 && bounds.height > 0) {
            this.normalBounds = bounds;
            setBounds(bounds);
        }
    }
}
