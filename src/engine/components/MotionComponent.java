/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.components;

import engine.Component;
import engine.Entity;

public class MotionComponent extends Component {
    private float x;
    private float y;
    private float oldX;
    private float oldY;
    private float vx;
    private float vy;
    private float maxSpeed;
    private float acceleration;
    private float deceleration;
    
    public MotionComponent(Entity entity, float x, float y, float maxSpeed) {
    	super(entity);
        this.x = this.oldX = x;
        this.y = this.oldY = y;
        this.maxSpeed = maxSpeed;
        this.vx = 0;
        this.vy = 0;
        this.acceleration = 1000f;
        this.deceleration = 1000f;
    }
    
    public void move(float deltaX, float deltaY) {
    	this.oldX = this.x;
    	this.oldY = this.y;
    	this.x += deltaX;
    	this.y += deltaY;
    }
    
	public float getX() { return x; }
    public float getY() { return y; }

	public void setX(float x) { this.x = x; }
	public void setY(float y) { this.y = y; }

	public float getOldX() { return oldX; }
    public float getOldY() { return oldY; }

	public void setOldX(float oldX) { this.oldX = oldX; }
	public void setOldY(float oldY) { this.oldY = oldY; }

	public float getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(float maxSpeed) { this.maxSpeed = maxSpeed; }

    public float getVx() { return vx; }
    public float getVy() { return vy; }

    public void setVx(float vx) { this.vx = vx; }
    public void setVy(float vy) { this.vy = vy; }
    
    public void addVx(float dvx) { this.vx += dvx; }
    public void addVy(float dvy) { this.vy += dvy; }

	public float getAcceleration() { return acceleration; }
	public float getDeceleration() { return deceleration; }

	public void setAcceleration(float acceleration) { this.acceleration = acceleration; }
	public void setDeceleration(float deceleration) { this.deceleration = deceleration; }

}
