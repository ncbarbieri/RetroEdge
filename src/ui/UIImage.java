/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class UIImage extends UIElement {
    private BufferedImage[] images;
    private float elapsedTime;
    private float frameDuration;
    private int currentFrame;
    private boolean looping;
    private boolean animating;
    private Runnable onAnimationEnd;
    private boolean moving;
    private int endX;
    private int endY;
    private float xSpeed;
    private float ySpeed;
    private Runnable onMotionEnd;

    public UIImage(int x, int y, int zIndex, String fileName) {
        super(x, y, zIndex);
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            images = new BufferedImage[1];
            images[0] = ImageIO.read(is);
            this.looping = false;
            this.animating = true;
            this.moving = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UIImage(int x, int y, int zIndex, UISpritesheet spritesheet, float frameDuration) {
        super(x, y, zIndex);
        this.images = spritesheet.getImages();
        this.elapsedTime = 0;
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.looping = false;
        this.animating = true;
        this.moving = false;
    }

    public UIImage(int x, int y, int zIndex, BufferedImage image) {
        super(x, y, zIndex);
        this.images = new BufferedImage[] { image };
        this.looping = false;
        this.animating = false;
        this.moving = false;
    }

    public void setImage(BufferedImage image) {
        this.images = new BufferedImage[] { image };
        this.currentFrame = 0;
        this.animating = false;
    }

    public void setDestination(int endX, int endY, float speed) {
        float dx = endX - getLocalX();
        float dy = endY - getLocalY();
        float mod = (float) Math.sqrt(dx*dx + dy*dy);
        xSpeed = dx/mod * speed;
        ySpeed = dy/mod * speed;
        this.endX = endX;
        this.endY = endY;
        this.moving = true;
    }

    public void startAnimation() {
        this.animating = true;
        this.currentFrame = 0;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public void setOnAnimationEnd(Runnable onAnimationEnd) {
        this.onAnimationEnd = onAnimationEnd;
    }

    public void setOnMotionEnd(Runnable onMotionEnd) {
        this.onMotionEnd = onMotionEnd;
    }

    @Override
    public void update(float deltaTime) {
        if (isVisible()) {
            // Animation
            if (animating && images != null && images.length > 1) {
                elapsedTime += deltaTime;
                if (elapsedTime >= frameDuration) {
                    elapsedTime -= frameDuration;
                    if ((currentFrame + 1) < images.length) {
                        currentFrame++;
                    } else if (looping) {
                        currentFrame = 0;
                    } else {
                        animating = false;
                        if (onAnimationEnd != null) onAnimationEnd.run();
                    }
                }
            }

            // Movement
            if (moving) {
                float newX = getLocalX() + xSpeed * deltaTime;
                float newY = getLocalY() + ySpeed * deltaTime;

                if ((xSpeed > 0 && newX > endX) || (xSpeed < 0 && newX < endX)) {
                    newX = endX;
                }

                if ((ySpeed > 0 && newY > endY) || (ySpeed < 0 && newY < endY)) {
                    newY = endY;
                }

                setX((int) newX);
                setY((int) newY);

                if (getLocalX() == endX && getLocalY() == endY) {
                    moving = false;
                    if (onMotionEnd != null) onMotionEnd.run();
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g, int xOffset, int yOffset) {
        if (isVisible() && images != null && images[currentFrame] != null) {
            int drawX = getGlobalX() ;
            int drawY = getGlobalY() ;
            if (this.useCameraOffsets) {
            	drawX -= xOffset;
            	drawY -= yOffset;
            }
            g.drawImage(images[currentFrame], drawX, drawY, null);
        }
    }
}