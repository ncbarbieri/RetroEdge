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

public class NPCComponent extends Component {
    private float targetX;
    private float targetY;
    private float xSpeed;
    private float ySpeed;
    private Runnable onTargetReached;

    public NPCComponent(Entity entity, float targetX, float targetY) {
        super(entity);
        onTargetReached = null;
        setTargetPosition(targetX, targetY);
    }

    public void setTargetPosition(float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.xSpeed = 0;
        this.ySpeed = 0;

        // Calcola la velocità in base alla posizione e alla velocità dell'entità
        if (entity.hasComponent(MotionComponent.class)) {
        	MotionComponent pc = entity.getComponent(MotionComponent.class);
            float dx = targetX - pc.getX();
            float dy = targetY - pc.getY();
            float mod = (float) Math.sqrt(dx * dx + dy * dy);

            // Usa la velocità dal PositionComponent
            float movementSpeed = pc.getMaxSpeed();
            xSpeed = dx / mod * movementSpeed;
            ySpeed = dy / mod * movementSpeed;
        }
    }

    public void setOnTargetReached(Runnable onTargetReached) {
        this.onTargetReached = onTargetReached;
    }

    public void onTargetReached() {
        if (onTargetReached != null) {
            onTargetReached.run();
        }
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public float getXSpeed() {
        return xSpeed;
    }

    public float getYSpeed() {
        return ySpeed;
    }
}