/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.components;

import java.awt.image.BufferedImage;
import engine.Component;
import engine.Entity;
import enums.Action;
import enums.Direction;
import world.CharacterSpritesheet;

public class SpriteComponent extends Component {
    protected int currentFrame;
    protected float frameDuration;
    protected float elapsedTime;
	protected int frameWidth;
	protected int frameHeight;
    protected Direction direction;
    protected Action action;
//  Flag indicating whether the animation should loop or play once
    protected boolean looping;         
    private Runnable onAnimationEnd;
    
	// Le immagini dell'animazione
    // images[action][direction][animation frame]
	protected BufferedImage[][][] images; 

    public SpriteComponent(Entity entity, CharacterSpritesheet spritesheet, float frameDuration, boolean looping) {
    	super(entity);

    	if (spritesheet == null) {
            throw new IllegalArgumentException("Spritesheet cannot be null.");
        }

        this.frameDuration = frameDuration;
        this.looping = looping;
        this.onAnimationEnd = null;
        this.currentFrame = 0;
        this.elapsedTime = 0.0f;
        this.direction = Direction.RIGHT; // Default direction
        this.action = Action.IDLE; // Default action
        this.images = spritesheet.getImages();
        this.frameWidth = spritesheet.getFrameWidth();
        this.frameHeight = spritesheet.getFrameHeight();
    }
    
    public BufferedImage getCurrentSprite() {
        if (!hasFrame(currentFrame)) {
            return null; // Nessun frame valido
        }
        return images[this.action.getActionIndex()][this.direction.getDirectionIndex()][this.currentFrame];
    }
    
	public boolean hasFrame(int index) {
	    if (images == null || images[this.action.getActionIndex()] == null ||
	            images[this.action.getActionIndex()][this.direction.getDirectionIndex()] == null) {
	        return false;
	    }
	    BufferedImage[] frames = images[this.action.getActionIndex()][this.direction.getDirectionIndex()];
	    return index < frames.length && frames[index] != null;
	}
	
	public int getLength() {
        if (images == null || images[this.action.getActionIndex()] == null ||
                images[this.action.getActionIndex()][this.direction.getDirectionIndex()] == null) {
            return 0;
        }
		return images[action.getActionIndex()][direction.getDirectionIndex()].length;	
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public int getCurrentFrame() {
		return currentFrame;
	}

	public void setCurrentFrame(int currentFrame) {
		this.currentFrame = currentFrame;
	}

	public float getFrameDuration() {
		return frameDuration;
	}

	public float getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(float elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public boolean isLooping() {
		return looping;
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
	}

	public Runnable getOnAnimationEnd() {
		return onAnimationEnd;
	}

	public void setOnAnimationEnd(Runnable onAnimationEnd) {
		this.onAnimationEnd = onAnimationEnd;
	}

	public void setFrameDuration(float frameDuration) {
		this.frameDuration = frameDuration;
	}
}