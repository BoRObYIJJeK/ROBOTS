package gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class FogOfWar {
    private final int widthPixels;
    private final int heightPixels;
    private final int cellSize;

    private BufferedImage staticFogImage;
    private BufferedImage dynamicFogImage;

    private static final int VISION_RADIUS_PIXELS = 65;

    private Point startPoint = null;
    private Point endPoint = null;

    public FogOfWar(int widthCells, int heightCells, int cellSize) {
        this.cellSize = cellSize;
        this.widthPixels = widthCells * cellSize;
        this.heightPixels = heightCells * cellSize;
        resetFog();
    }

    private void resetFog() {
        staticFogImage = new BufferedImage(widthPixels, heightPixels, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = staticFogImage.createGraphics();
        g.setColor(new Color(15, 15, 15, 255));
        g.fillRect(0, 0, widthPixels, heightPixels);
        g.dispose();

        dynamicFogImage = new BufferedImage(widthPixels, heightPixels, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gd = dynamicFogImage.createGraphics();
        gd.setColor(new Color(15, 15, 15, 255));
        gd.fillRect(0, 0, widthPixels, heightPixels);
        gd.dispose();
    }

    public void initSpecialPoints(Point start, Point end) {
        this.startPoint = start;
        this.endPoint = end;
    }

    public void updatePixels(int robotX, int robotY, int difficulty) {
        if (difficulty == 0) return;

        if (difficulty == 2) {
            dynamicFogImage = new BufferedImage(widthPixels, heightPixels, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dynamicFogImage.createGraphics();
            g.setColor(new Color(15, 15, 15, 255));
            g.fillRect(0, 0, widthPixels, heightPixels);

            g.setComposite(AlphaComposite.Clear);
            g.fillOval(robotX - VISION_RADIUS_PIXELS, robotY - VISION_RADIUS_PIXELS,
                    VISION_RADIUS_PIXELS * 2, VISION_RADIUS_PIXELS * 2);
            g.dispose();
        }
        else if (difficulty == 1) {
            Graphics2D g = staticFogImage.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillOval(robotX - VISION_RADIUS_PIXELS, robotY - VISION_RADIUS_PIXELS,
                    VISION_RADIUS_PIXELS * 2, VISION_RADIUS_PIXELS * 2);
            g.dispose();
        }
    }

    /**
     * Считывает текстуру тумана попиксельно и кодирует в строку:
     * '1' - черный пиксель тумана, '0' - прозрачный (стертый) пиксель.
     */
    public String getFogPixelsString(int difficulty) {
        // Выбираем правильный буфер в зависимости от сложности
        BufferedImage img = (difficulty == 2) ? dynamicFogImage : staticFogImage;

        if (img == null) {
            img = staticFogImage;
        }

        StringBuilder sb = new StringBuilder(widthPixels * heightPixels);

        // Получаем альфа-канал пикселей напрямую из растра изображения,
        // это полностью исключает любые искажения цвета в headless-режиме тестов
        java.awt.image.WritableRaster raster = img.getAlphaRaster();

        for (int y = 0; y < heightPixels; y++) {
            for (int x = 0; x < widthPixels; x++) {
                if (raster != null) {
                    int alpha = raster.getSample(x, y, 0);
                    // Если альфа меньше 10 (пиксель прозрачный/стертый), пишем '0', иначе '1' (туман)
                    sb.append(alpha < 10 ? "0" : "1");
                } else {
                    // Резервный вариант, если растр недоступен
                    int rgb = img.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xff;
                    sb.append(alpha < 10 ? "0" : "1");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Восстанавливает попиксельную текстуру тумана из битовой строки JSON
     */
    public void restoreFromPixelsString(String fogPixelsData, int difficulty) {
        resetFog();
        if (fogPixelsData == null || fogPixelsData.length() < widthPixels * heightPixels) return;

        int index = 0;
        // Заполняем картинку статического тумана сохраненными пикселями
        for (int y = 0; y < heightPixels; y++) {
            for (int x = 0; x < widthPixels; x++) {
                boolean isFog = (fogPixelsData.charAt(index) == '1');
                int color = isFog ? new Color(15, 15, 15, 255).getRGB() : new Color(0, 0, 0, 0).getRGB();
                staticFogImage.setRGB(x, y, color);
                index++;
            }
        }
    }

    public void draw(Graphics2D g, int difficulty) {
        if (difficulty == 0) return;
        BufferedImage currentFog = (difficulty == 2) ? dynamicFogImage : staticFogImage;
        if (currentFog != null) {
            g.drawImage(currentFog, 0, 0, null);
        }
    }
}
