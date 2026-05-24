package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import log.Logger;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается.
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private LogWindow logWindow;
    private GameWindow gameWindow;

    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width  - inset*2,
                screenSize.height - inset*2);

        setContentPane(desktopPane);


        logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow();
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

        setJMenuBar(generateMenuBar());

        // Окно с подтверждением закрытия
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });

        // Восстановление профиля
        checkAndRestoreProfile();
    }

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С ПРОФИЛЕМ ==========

    private void checkAndRestoreProfile() {
        if (ProfileManager.hasProfile() && ProfileManager.askRestoreProfile(this)) {
            // 1. Восстанавливаем геометрию окон (размеры и координаты)
            ProfileManager.WindowState state = ProfileManager.loadProfile();
            ProfileManager.applyProfile(state, this, logWindow, gameWindow);

            // 2. Восстанавливаем лабиринт, робота и туман войны
            if (GameProgressManager.hasProgress() && gameWindow != null) {
                GameProgressManager.GameState progressState = GameProgressManager.loadProgress();

                if (progressState != null) {
                    // Восстанавливаем игру и туман внутри визуализатора
                    GameProgressManager.applyProgress(progressState, gameWindow.getVisualizer());

                    // Автоматически переключаем галочку в верхнем меню на нужную сложность
                    try {
                        JMenuBar menuBar = this.getJMenuBar();
                        JMenu mazeMenu = menuBar.getMenu(3); // Меню "Лабиринт"
                        JMenu diffMenu = (JMenu) mazeMenu.getItem(2); // Подменю "Сложность"
                        JRadioButtonMenuItem targetItem = (JRadioButtonMenuItem) diffMenu.getItem(progressState.difficulty);
                        targetItem.setSelected(true);
                    } catch (Exception e) {
                        // Игнорируем ошибку, если структура меню изменилась
                    }
                }
            }
        }
    }

    private void confirmExit() {
        ExitManager.confirmExit(this, logWindow, gameWindow);
    }

    // =================================================

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");

        return logWindow;
    }

    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

//    protected JMenuBar createMenuBar() {
//        JMenuBar menuBar = new JMenuBar();
//
//        //Set up the lone menu.
//        JMenu menu = new JMenu("Document");
//        menu.setMnemonic(KeyEvent.VK_D);
//        menuBar.add(menu);
//
//        //Set up the first menu item.
//        JMenuItem menuItem = new JMenuItem("New");
//        menuItem.setMnemonic(KeyEvent.VK_N);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_N, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("new");
    ////        menuItem.addActionListener(this);
//        menu.add(menuItem);
//
//        //Set up the second menu item.
//        menuItem = new JMenuItem("Quit");
//        menuItem.setMnemonic(KeyEvent.VK_Q);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_Q, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("quit");
//        ////        menuItem.addActionListener(this);
//        menu.add(menuItem);
//
//        return menuBar;
//    }

    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu()); // Пункт меню "Выйти"
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createMazeMenu());
        return menuBar;
    }
    private JMenu createLookAndFeelMenu()
    {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);
        return lookAndFeelMenu;

    }

    private JMenu createTestMenu()
    {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");

        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> {
            Logger.debug("Новая строка");
        });
        testMenu.add(addLogMessageItem);
        return testMenu;
    }
    // Пункт меню "Выйти"
    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exitItem = new JMenuItem("Выйти", KeyEvent.VK_X);
        exitItem.addActionListener((event) -> confirmExit());

        fileMenu.add(exitItem);
        return fileMenu;
    }

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }

    private JMenu createMazeMenu() {
        JMenu mazeMenu = new JMenu("Лабиринт");
        mazeMenu.setMnemonic(KeyEvent.VK_L);

        JMenuItem newMazeItem = new JMenuItem("Новый лабиринт", KeyEvent.VK_N);
        newMazeItem.addActionListener(e -> {
            gameWindow.generateNewMaze();
        });
        mazeMenu.add(newMazeItem);

        // Разделитель в меню
        mazeMenu.addSeparator();

        // Создаем подменю "Сложность"
        JMenu difficultyMenu = new JMenu("Сложность");

        // Группа кнопок, чтобы одновременно можно было выбрать только один режим
        ButtonGroup difficultyGroup = new ButtonGroup();

        // 1-й вариант
        JRadioButtonMenuItem normalMode = new JRadioButtonMenuItem("1. Обычный лабиринт", true);
        normalMode.addActionListener(e -> {
            if (gameWindow != null) gameWindow.getVisualizer().setDifficulty(0);
        });

        // 2-й вариант
        JRadioButtonMenuItem staticFogMode = new JRadioButtonMenuItem("2. Туман войны (исследуемый)");
        staticFogMode.addActionListener(e -> {
            if (gameWindow != null) gameWindow.getVisualizer().setDifficulty(1);
        });

        // 3-й вариант
        JRadioButtonMenuItem dynamicFogMode = new JRadioButtonMenuItem("3. Туман войны (вокруг робота)");
        dynamicFogMode.addActionListener(e -> {
            if (gameWindow != null) gameWindow.getVisualizer().setDifficulty(2);
        });

        // Объединяем кнопки в группу
        difficultyGroup.add(normalMode);
        difficultyGroup.add(staticFogMode);
        difficultyGroup.add(dynamicFogMode);

        // Добавляем кнопки в подменю
        difficultyMenu.add(normalMode);
        difficultyMenu.add(staticFogMode);
        difficultyMenu.add(dynamicFogMode);

        // Добавляем подменю в основное меню "Лабиринт"
        mazeMenu.add(difficultyMenu);

        return mazeMenu;
    }
}