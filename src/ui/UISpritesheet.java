/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package ui;

import java.awt.image.BufferedImage;

import world.Spritesheet;

public class UISpritesheet extends Spritesheet {
    private BufferedImage[] images;

    public UISpritesheet(String fileName, int frameWidth, int frameHeight) {
        super(fileName, frameWidth, frameHeight);
        createImageArray(); // Inizializza l'array nel costruttore
    }

    public BufferedImage getFrame(int frameIndex) {
        if (images != null && frameIndex >= 0 && frameIndex < images.length) {
            return images[frameIndex];
        }
        throw new IndexOutOfBoundsException("Frame index out of range: " + frameIndex);
    }

    /**
     * Crea l'array di immagini dal spritesheet e lo assegna a `images`.
     */
    private void createImageArray() {
        int totalFrames = getRows() * getCols();
        images = new BufferedImage[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            int row = i / getCols();
            int col = i % getCols();
            images[i] = getSprite(row, col);
        }
    }

    /**
     * Restituisce l'array di immagini.
     * 
     * @return L'array di immagini estratte dal spritesheet.
     */
    public BufferedImage[] getImages() {
        return images;
    }
}