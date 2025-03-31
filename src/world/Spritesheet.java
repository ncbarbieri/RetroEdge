/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package world;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class Spritesheet {
    protected BufferedImage spriteSheet;
    protected BufferedImage[][] sprites;
    protected int rows;
    protected int cols;
    protected int frameWidth;
    protected int frameHeight;

    public Spritesheet() {
        // Costruttore vuoto per inizializzazione a posteriori
    }

    public Spritesheet(String fileName, int frameWidth, int frameHeight, int margin, int spacing) {
        initialize(fileName, frameWidth, frameHeight, margin, spacing);
    }

    public Spritesheet(String fileName, int frameWidth, int frameHeight) {
    	initialize(fileName, frameWidth, frameHeight, 0, 0);
    }

    public void initialize(String fileName, int frameWidth, int frameHeight, int margin, int spacing) {
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            this.spriteSheet = ImageIO.read(is);
            int width = spriteSheet.getWidth();
            int height = spriteSheet.getHeight();
            this.rows = (height - margin * 2) / (frameHeight + spacing);
            this.cols = (width - margin * 2) / (frameWidth + spacing);
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            loadSprites(margin, spacing);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing spritesheet: " + fileName);
        }
    }
    
    private void loadSprites(int margin, int spacing) {
        sprites = new BufferedImage[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                sprites[row][col] = spriteSheet.getSubimage(
                        margin + col * (frameWidth + spacing),
                        margin + row * (frameHeight + spacing),
                        frameWidth,
                        frameHeight);
            }
        }
    }

    public BufferedImage getSprite(int row, int col) {
        return sprites[row][col];
    }

	public BufferedImage getFlippedSprite(int row, int col) {
		int width = sprites[row][col].getWidth();
		int height = sprites[row][col].getHeight();
		AffineTransform flipTx = new AffineTransform(-1., 0., 0., 1., (double) width, 0.);
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.transform(flipTx);
		g.drawImage(sprites[row][col], 0, 0, null);
		g.dispose();
		return newImage;
	}

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }
}