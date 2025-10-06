/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.systems;

import java.awt.Rectangle;
import engine.BaseSystem;
import engine.Engine;
import engine.components.AttackComponent;
import engine.components.ColliderComponent;
import engine.components.HealthComponent;
import engine.components.MotionComponent;
import engine.components.SpriteComponent;
import engine.Entity;
import enums.EngineState;
import helpers.Logger;

public class AttackSystem extends BaseSystem {

    public AttackSystem(Engine engine) {
        super(engine, 5); // Priority level
    }

    @Override
    protected void initStateUpdateMap() {
        // Customize states where this system updates
    	setUpdateInState(EngineState.STARTING, false);
    	setUpdateInState(EngineState.RUNNING, true);
    	setUpdateInState(EngineState.CUTSCENE, false);
    	setUpdateInState(EngineState.PAUSED, false);
    	setUpdateInState(EngineState.SHOWING_DIALOG, false);
    	setUpdateInState(EngineState.EXITING, false);
    	setUpdateInState(EngineState.ENTERING, false);
    }

    @Override
    public void update(float deltaTime) {
        // Aggiorna le collisioni tra entità e tile
        engine.accessEntities(entities -> {
            int size = entities.size();
            for (Entity attacker : entities) {
            	if (attacker.hasComponent(AttackComponent.class)) {
            		AttackComponent attackComponent = attacker.getComponent(AttackComponent.class);
            		processAttacker(attackComponent, deltaTime);
            		if (attackComponent.isAttacking() && !attackComponent.isAttackApplied()) {
                        // Perform attack logic (e.g., collision detection)
                        for (int i = 0; i < size; i++) {
                            Entity target = entities.get(i);
                            if (target.equals(attacker)) {
                                continue; // Skip self
                            }
                            updateEntityPair(attacker, target, deltaTime);
                        }
                    }
            	}
            }
        });
    }

    protected void updateEntityPair(Entity attacker, Entity target, float deltaTime) {
		AttackComponent attackComponent = attacker.getComponent(AttackComponent.class);
		MotionComponent mc = attacker.getComponent(MotionComponent.class);
        SpriteComponent sc = attacker.getComponent(SpriteComponent.class);
        Rectangle hitBox = attackComponent.getHitBox(sc.getDirection().getDirectionIndex());
        if (hitBox == null) {
            return;
        }
        hitBox.x += (int) mc.getX();
        hitBox.y += (int) mc.getY();

        if (checkCollision(attacker, target, hitBox)) {
            applyAttack(attackComponent, target);
            return; // Stop checking after the first collision
        }
    }
    
    private void processAttacker(AttackComponent attackComponent, float deltaTime) {
        if (attackComponent.isAttacking()) {

            // Update attack animation timer
            attackComponent.updateAttack(deltaTime);

            if (attackComponent.isAttackComplete()) {
                // End the attack if the animation is complete
                attackComponent.stopAttack();
            }
        } else {
            // Update cooldown timer if not attacking
            attackComponent.updateCooldown(deltaTime);
        }
    }

    private boolean checkCollision(Entity attacker, Entity target, Rectangle hitBox) {
    	MotionComponent position = target.getComponent(MotionComponent.class);
        ColliderComponent collider = target.getComponent(ColliderComponent.class);

        if (position == null || collider == null) {
            return false; // Target cannot be collided with
        }

        Rectangle solidArea = collider.getBoundingBox();
        solidArea.x += position.getX();
        solidArea.y += position.getY();

        return hitBox.intersects(solidArea);
    }

    private void applyAttack(AttackComponent attackComponent, Entity target) {
        HealthComponent health = target.getComponent(HealthComponent.class);
        if (health != null) {
            health.decreaseHealth(attackComponent.getDamage());
            attackComponent.setAttackApplied(true);
            Logger.log("Applied attack: -" + attackComponent.getDamage() + " health to entity: " + target.getId());
        }
    }
}