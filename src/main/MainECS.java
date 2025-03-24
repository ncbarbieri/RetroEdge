package main;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.JFrame;
import engine.Engine;
import engine.GameEngine;
import helpers.Logger;
import input.InputHandler;
import input.MouseInputHandler;

public class MainECS {

    private static final String GAME_TITLE = "Game Tutorial";

    public static void main(String[] args) {
    	InputHandler inputHandler = new InputHandler();
    	MouseInputHandler mouseInputHandler = new MouseInputHandler();
    	Engine engine = new GameEngine(inputHandler, mouseInputHandler);
        GamePanel gamePanel = new GamePanel(engine);
        JFrame window = createGameWindow(GAME_TITLE, gamePanel, engine);
        window.addKeyListener(inputHandler);
        window.addMouseListener(mouseInputHandler);
        window.addMouseMotionListener(mouseInputHandler);
        addListeners(window, gamePanel, engine);
        window.setVisible(true);
    }

    private static JFrame createGameWindow(String title, GamePanel gamePanel, Engine engine) {
        JFrame window = new JFrame(title);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        return window;
    }

    private static void addListeners(JFrame window, GamePanel gamePanel, Engine engine) {
        window.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                gamePanel.windowGainedFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                gamePanel.windowFocusLost();
            }
        });

        window.addKeyListener(new KeyAdapter() {
            private boolean isFullScreen = false;

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F) {
                	toggleFullScreen(window);
                }
                
            }

            private void toggleFullScreen(JFrame window) {
                GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                if (!isFullScreen) {
                    configureFullScreen(window, device);
                    Logger.log("Switched to Full-Screen Mode");
                } else {
                    exitFullScreen(window, device);
                    Logger.log("Exited Full-Screen Mode");
                }
                isFullScreen = !isFullScreen;
            }

            private void configureFullScreen(JFrame window, GraphicsDevice device) {
                window.dispose();
                window.setUndecorated(true);
                device.setFullScreenWindow(window);
                window.setVisible(true);
            }

            private void exitFullScreen(JFrame window, GraphicsDevice device) {
                device.setFullScreenWindow(null);
                window.dispose();
                window.setUndecorated(false);
                window.setLocationRelativeTo(null);
                window.setVisible(true);
            }
        });

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeGame(window, gamePanel, engine);
            }
        });
    }

    private static void closeGame(JFrame window, GamePanel gamePanel, Engine engine) {
    	Logger.log("Closing game...");
    	gamePanel.stopGameLoop();
        engine.cleanup();
        window.dispose();
        System.exit(0);
    }
}