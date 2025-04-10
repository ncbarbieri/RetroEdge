/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.systems;

import engine.BaseSystem;
import engine.Engine;
import engine.components.AttackComponent;
import engine.components.GravityComponent;
import engine.components.KeyInputComponent;
import engine.components.MotionComponent;
import engine.components.NPCComponent;
import engine.components.ThrowProjectileComponent;
import engine.Entity;
import enums.EngineState;
import input.InputAction;

public class MotionSystem extends BaseSystem {

    public MotionSystem(Engine engine) {
        super(engine, 2); // Priorità 2
    }

    @Override
    protected void initStateUpdateMap() {
        // Customize states where this system updates
    	setUpdateInState(EngineState.STARTING, false);
    	setUpdateInState(EngineState.RUNNING, true);
    	setUpdateInState(EngineState.CUTSCENE, true);
    	setUpdateInState(EngineState.PAUSED, false);
    	setUpdateInState(EngineState.SHOWING_DIALOG, false);
    	setUpdateInState(EngineState.EXITING, false);
    	setUpdateInState(EngineState.ENTERING, true);
    }

    @Override
    protected void updateEntity(Entity entity, float deltaTime) {
    	MotionComponent mc = entity.getComponent(MotionComponent.class);

        if (mc == null) return;

        AttackComponent ac = entity.getComponent(AttackComponent.class);
        ThrowProjectileComponent tpc = entity.getComponent(ThrowProjectileComponent.class);

        boolean isPerformingAction = (ac != null && ac.isAttacking()) || 
                                     (tpc != null && tpc.isThrowing());
        if (isPerformingAction) {
            // Blocca completamente il movimento
            mc.setVx(0);
            mc.setVy(0);

            // Aggiorna la posizione
            mc.move(0, 0);
            return;
        }
        
        if (entity.hasComponent(KeyInputComponent.class)) {
            handlePlayerMovement(entity, deltaTime, mc);
            return;
        }

        if (entity.hasComponent(NPCComponent.class)) {
            handleNPCMovement(entity, deltaTime);
            return;
        }
        // Gestione generica per altre entità
        handleGenericMovement(entity, deltaTime, mc);
    }

    private void handlePlayerMovement(Entity entity, float deltaTime, MotionComponent mc) {
        KeyInputComponent keyInput = entity.getComponent(KeyInputComponent.class);
        GravityComponent gc = entity.getComponent(GravityComponent.class);
        AttackComponent ac = entity.getComponent(AttackComponent.class);

        float maxSpeed = mc.getMaxSpeed();
        float vx = mc.getVx();
        float vy = mc.getVy();
        float acceleration = mc.getAcceleration();
        float deceleration = mc.getDeceleration();

        boolean moveLeft = keyInput.isActionActive(InputAction.MOVE_LEFT);
        boolean moveRight = keyInput.isActionActive(InputAction.MOVE_RIGHT);
        boolean moveUp = keyInput.isActionActive(InputAction.MOVE_UP);
        boolean moveDown = keyInput.isActionActive(InputAction.MOVE_DOWN);

        // Accelerazione orizzontale
        if (moveLeft && !moveRight) {
            vx -= acceleration * deltaTime;
        } else if (moveRight && !moveLeft) {
            vx += acceleration * deltaTime;
        } else {
            // Decelerazione orizzontale
                if (vx > 0) {
                    vx -= deceleration * deltaTime;
                    if (vx < 0) vx = 0;
                } else if (vx < 0) {
                    vx += deceleration * deltaTime;
                    if (vx > 0) vx = 0;
                }
        }

        // Accelerazione verticale
        if (gc != null) {
            // Platformer scenario
            // Verifica velocità per movimento orizzontale
            if (Math.abs(vx) > maxSpeed)
            	vx = (vx>0?maxSpeed:-maxSpeed);
            	

            // Jump if requested and on ground
            if (keyInput.isActionActive(InputAction.JUMP) && !gc.isInAir()) {
                gc.jump();
            }
            gc.updateAirSpeed(deltaTime);
            vy = gc.getAirSpeed();
        } else {
            // Top-down scenario (no gravity)
            if (moveUp && !moveDown) {
                vy -= acceleration * deltaTime;
            } else if (moveDown && !moveUp) {
                vy += acceleration * deltaTime;
            } else {
                // Decelerazione verticale
                    if (vy > 0) {
                        vy -= deceleration * deltaTime;
                        if (vy < 0) vy = 0;
                    } else if (vy < 0) {
                        vy += deceleration * deltaTime;
                        if (vy > 0) vy = 0;
                    }
            }
            
            // Verifica velocità totale per movimento diagonale
            float speed = (float) Math.sqrt(vx * vx + vy * vy);
            if (speed > maxSpeed) {
                float factor = maxSpeed / speed;
                vx *= factor;
                vy *= factor;
            }
        }

        // Aggiorna la velocità nel componente
        mc.setVx(vx);
        mc.setVy(vy);

        // Aggiorna la posizione
        mc.move(vx * deltaTime, vy * deltaTime);

        // Gestione dell'attacco
        if (ac != null && keyInput.isActionActive(InputAction.ATTACK) && ac.canAttack()) {
            ac.startAttack();
        }
    }
    
    private void handleNPCMovement(Entity entity, float deltaTime) {
    	MotionComponent mc = entity.getComponent(MotionComponent.class);
        NPCComponent npc = entity.getComponent(NPCComponent.class);

        float currentX = mc.getX();
        float currentY = mc.getY();
        float targetX = npc.getTargetX();
        float targetY = npc.getTargetY();

        float dx = targetX - currentX;
        float dy = targetY - currentY;

        // Check if NPC reached target
        boolean reachedTarget = Math.abs(dx) <= Math.abs(npc.getXSpeed()*deltaTime) &&
                                Math.abs(dy) <= Math.abs(npc.getYSpeed()*deltaTime);

        if (reachedTarget) {
            // Final adjustment
            dx = targetX - currentX;
            dy = targetY - currentY;
            npc.onTargetReached();
        } else {
            // Move towards target
            // Normalize or just use XSpeed, YSpeed directly
            dx = npc.getXSpeed() * deltaTime;
            dy = npc.getYSpeed() * deltaTime;
        }

        // Set velocity:
        mc.setVx(dx / deltaTime); // convert displacement per frame back to velocity
        mc.setVy(dy / deltaTime);

        mc.move(dx, dy); // This sets deltaX, deltaY as well
    }
    
    private void handleGenericMovement(Entity entity, float deltaTime, MotionComponent mc) {
        float vx = mc.getVx();
        float vy = mc.getVy();

        mc.move(vx * deltaTime, vy * deltaTime);
    }
}