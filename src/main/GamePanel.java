/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import engine.Engine;
import helpers.Logger;

public class GamePanel extends JPanel implements Runnable {
    private static final long serialVersionUID = 1L;
    private static final int FPS = 60;
    private static final int UPS = 60;

    private Thread gameThread;
    private volatile boolean isRunning = true; // Controlla lo stato del ciclo di gioco
    private static long lastCheck;
    private static int frames, updates, fps, ups;
    private Engine engine;
    private float scaleX = 1.0f, scaleY = 1.0f;

    public static final int GAME_WIDTH = 640;
    public static final int GAME_HEIGHT = 480;
	private final Font arial = new Font("Arial", Font.PLAIN, 12);
	
    public GamePanel(Engine engine) {
        this.engine = engine;
        this.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        addResizeListener();
        startGameLoop();
    }

    private void addResizeListener() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateScale();
            }
        });
    }

    private void updateScale() {
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        scaleX = (float) panelWidth / GAME_WIDTH;
        scaleY = (float) panelHeight / GAME_HEIGHT;
    }

    public void windowFocusLost() {
        engine.windowFocusLost();
    }

    public void windowGainedFocus() {
        engine.windowGainedFocus();
    }

    private void startGameLoop() {
        this.gameThread = new Thread(this);
        this.gameThread.start();
    }

    public void stopGameLoop() {
        isRunning = false;
        try {
            if (gameThread != null) gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("Game loop interrupted: " + e.getMessage());
        }
    }
	
	@Override
	public void run() {
	    final double updateInterval = 1.0 / UPS; // Tempo per ogni aggiornamento logico
	    final double frameInterval = 1.0 / FPS; // Tempo per ogni frame renderizzato

	    lastCheck = System.currentTimeMillis();
	    updates = 0;
	    frames = 0;

	    double deltaU = 0, deltaF = 0;
	    long previousTime = System.nanoTime();

	    while (isRunning) {
	        long currentTime = System.nanoTime();
	        double delta = (currentTime - previousTime) / 1_000_000_000.0; // In secondi
	        previousTime = currentTime;

	        deltaU += delta;
	        deltaF += delta;

	        // Aggiorna logica di gioco
	        while (deltaU >= updateInterval) {
	            updateGame((float)updateInterval);
	            if (engine.isDebug()) updates++;
	            deltaU -= updateInterval; // Sottrai il valore corretto
	        }

	        // Renderizza il gioco
	        if (deltaF >= frameInterval) {
	            repaint();
	            if (engine.isDebug()) frames++;
	            deltaF -= frameInterval; // Sottrai il valore corretto
	        }

	        // Aggiorna debug
	        if (engine.isDebug()) {
	            updateDebug();
	        }

	        // Ottimizza il consumo della CPU
	        try {
	            Thread.sleep(1);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
    private void updateDebug() {
        if (System.currentTimeMillis() - lastCheck >= 1000) {
            lastCheck = System.currentTimeMillis();
            fps = frames;
            ups = updates;
			frames = 0;
			updates = 0;
        }
    }

	public void updateGame(float deltaTime) {
		this.engine.update(deltaTime);
	}
	
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.scale(scaleX, scaleY);

        // Verifica se siamo sul thread EDT
        if (!SwingUtilities.isEventDispatchThread()) {
            Logger.log("paintComponent not on EDT. Current thread: " + Thread.currentThread().getName());
        }
        
        engine.render(g2D);

        if (engine.isDebug()) drawDebugInfo(g);
        g.dispose();
    }

    public static void resetDebug() {
        frames=updates=ups=fps=0;
    }

    private void drawDebugInfo(Graphics g) {
        g.setFont(arial);
        g.setColor(Color.white);
        String debugInfo = engine.getDebugInfo();
        g.drawString(ups + " UPS - " + fps + " FPS | " + debugInfo, 10, GAME_HEIGHT - 10);
    }
}