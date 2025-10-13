/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package ui.elements;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import engine.components.MotionComponent;
import engine.components.ColliderComponent;
import ui.UIElement;
import ui.UISpritesheet;
import engine.Entity;

public class UINotification extends UIElement {
    private BufferedImage[] frames;
    private int currentFrame;
    private float frameDuration;
    private float elapsedTime;
    private Entity trackedEntity;
    private int offsetX, offsetY;

    public UINotification(int offsetX, int offsetY, int zIndex, Entity entity, UISpritesheet spritesheet, float frameDuration) {
        super(0, 0, zIndex);
        this.frames = spritesheet.getImages();
        this.trackedEntity = entity;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.elapsedTime = 0f;

        // This element tracks an entity in the game world, so it should use camera offsets
        setUseCameraOffsets(true);
    }

    @Override
    public void update(float deltaTime) {
        if (!isVisible() || frames == null || frames.length == 0) return;
        elapsedTime += deltaTime;
        if (elapsedTime >= frameDuration) {
            elapsedTime = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }

    @Override
    public void render(Graphics2D g, int cameraX, int cameraY) {
        if (!isVisible() || frames == null || frames[currentFrame] == null) return;

        MotionComponent position = trackedEntity.getComponent(MotionComponent.class);
        ColliderComponent collider = trackedEntity.getComponent(ColliderComponent.class);
        if (position == null || collider == null) return;

        // Get Bounding Box
        Rectangle boundingBox = collider.getBoundingBox();

        // Convert world position to element’s position
        int worldX = (int) position.getX() + boundingBox.x + offsetX;
        int worldY = (int) position.getY() + boundingBox.y + offsetY;
        

        // If usesCameraOffsets is true, subtract camera offsets
        if (usesCameraOffsets()) {
            worldX -= cameraX;
            worldY -= cameraY;
        }

        g.drawImage(frames[currentFrame], worldX, worldY, null);
    }

    public void resetAnimation() {
        this.currentFrame = 0;
        this.elapsedTime = 0f;
    }
}