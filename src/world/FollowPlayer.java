/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package world;

import engine.components.MotionComponent;
import engine.components.SpriteComponent;
import engine.Entity;
import main.GamePanel;

public class FollowPlayer implements Camera {
	private MotionComponent pc;
	private int xOffset;
	private int yOffset;
	private int leftBorder;
	private int rightBorder;
	private int topBorder;
	private int bottomBorder;
	private int maxOffsetX;
	private int maxOffsetY;
	private int frameWidth;
	private int frameHeight;

	public FollowPlayer(TileMap tileManager, Entity entity) {
		this.pc = entity.getComponent(MotionComponent.class);
		this.frameWidth = entity.getComponent(SpriteComponent.class).getFrameWidth();
		this.frameHeight = entity.getComponent(SpriteComponent.class).getFrameHeight();
		this.leftBorder = (int) (0.2 * GamePanel.GAME_WIDTH);
		this.rightBorder = (int) (0.8 * GamePanel.GAME_WIDTH) - frameWidth;
		this.topBorder = (int) (0.2 * GamePanel.GAME_HEIGHT);
		this.bottomBorder = (int) (0.8 * GamePanel.GAME_HEIGHT) - frameHeight;
		this.maxOffsetX = (tileManager.getMapWidth() - tileManager.getVisibleCols()) * tileManager.getTileWidth();
		this.maxOffsetY = (tileManager.getMapHeight() - tileManager.getVisibleRows()) * tileManager.getTileHeight();
		this.xOffset = (int) (pc.getX() - GamePanel.GAME_WIDTH/2);
		this.yOffset = (int) (pc.getY() - GamePanel.GAME_HEIGHT/2);
	}
	
	public void update(float deltaTime) {
		int diffX = (int) this.pc.getX() - this.xOffset;
		if(diffX>this.rightBorder)
			this.xOffset += diffX - this.rightBorder;
		else if(diffX<this.leftBorder)
			this.xOffset += diffX - this.leftBorder;
		if(this.xOffset > this.maxOffsetX)
			this.xOffset = this.maxOffsetX;
		else if(this.xOffset < 0)
			this.xOffset = 0;
		
		int diffY = (int) this.pc.getY() - this.yOffset;
		if(diffY>this.bottomBorder)
			this.yOffset += diffY - this.bottomBorder;
		else if(diffY<this.topBorder)
			this.yOffset += diffY - this.topBorder;
		if(this.yOffset > this.maxOffsetY)
			this.yOffset = this.maxOffsetY;
		else if(this.yOffset < 0)
			this.yOffset = 0;
	}

	public int getxOffset() {
		return xOffset;
	}

	public int getyOffset() {
		return yOffset;
	}

}
