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
    /* Explicit Euler method:
     *  F = m*a -> a = F/m
     *  so -> acc = sum_forces/mass
     *  calculate position and velocity of the next frame
     *  pos[n+1] = pos[n] + v[n] * dt
     *  v[n+1] = v[n] + a * dt (it depends on the direction)
     *  pos can be either x or y
     *  not accurate
     *  the system gain energy: the ball bounces higher each frame
     *  when the ball hits a wall,
     *  the projection method is used
     *  so v[n+1] = -v[n] (in the direction of collision)
     * Semi-implicit Euler Method:
     *  calculate next velocity and then 
     *  use it to calculate the position
     *  more accurate, the system doesn't gain enery
     *  v[n+1] = v[n] + a * dt
     *  pos[n+1] = pos[n] + v[n+1] * dt
     * Mid-Point method:
     *  it uses dt/2
     * Runge-Kutta Method (RK-4):
     *  order 4 integration method
     *  it uses 4 k
     *  very accurate but computationally expensive
     * Verlet Integration:
     *  accurate and fast
     *  property: numerical stability and time reversibility
     *  it is used especially when 
     *  we have a system of several points 
     *  connected and constrained with each other
     *  instead of storing position and velocity
     *  current position and old position are stored
     *  the velocity is given by the two position
     *  pos[n] = pos[n-1] + v[n] dt
     *  v[n] = (pos[n] - pos[n-1])/dt
     *  pos[n+1] = pos[n] + (v[n] + a * dt) * dt
     *  that is
     *  pos[n+1] = pos[n] + v[n] * dt + a * dt^2
     *  that is
     *  pos[n+1] = pos[n] + ((pos[n] - pos[n-1])/dt) * dt + a * dt^2
     *  that is
     *  pos[n+1] = pos[n] + (pos[n] - pos[n-1]) + a * dt^2
     *  (Verlet integration formula)
     *  when the ball hits a wall
     *  the old position is changed to change velocity
     *  old_x = x + vel_x
     *  it's a hack, it's not perfect
     *  Stick(contraint): 
     *  a certain distance (length) between two points
     *  must be maintained (constant, not spring)
     *  dx = p1.x - p0.x
     *  dy = p1.y - p0.y
     *  distance = sqrt(dx^2+dy^2)
     *  diff = length - distance 
     *  percent = (diff - distance) / 2
     *  (divided by 2 because
     *  it is applied to both points)
     *  offset_x = dx * percent
     *  offset_y = dy * percent
     *  p0.x -= offset_x
     *  p0.y -= offset_y
     *  p1.x += offset_x
     *  p1.y += offset_y
     *  
     *  Anchor point:
     *  a point can be static (it cannot move)
     *  if a point is static, it's not updated
     *  https://johanpeitz.com/
     */

}