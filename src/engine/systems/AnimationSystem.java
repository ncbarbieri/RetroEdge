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
import engine.components.SpriteComponent;
import engine.components.TileMapComponent;
import engine.components.ThrowProjectileComponent;
import engine.components.AttackComponent;
import engine.components.GravityComponent;
import engine.components.MotionComponent;
import engine.Entity;
import enums.Action;
import enums.Direction;
import enums.EngineState;

public class AnimationSystem extends BaseSystem {

	public AnimationSystem(Engine engine) {
		super(engine, 9);
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
		if (entity.hasComponent(MotionComponent.class) && entity.hasComponent(SpriteComponent.class)) {
			SpriteComponent sprite = entity.getComponent(SpriteComponent.class);
	
			AttackComponent attack = entity.getComponent(AttackComponent.class);
			if (attack != null && attack.isAttacking()) {
				handleAttackAnimation(sprite, attack);
				return;
			}
			
			ThrowProjectileComponent throwProjectile = entity.getComponent(ThrowProjectileComponent.class);
			if (throwProjectile!= null && throwProjectile.isThrowing()) {
				handleThrowAnimation(sprite, throwProjectile);
				return;
			}

			// Determine direction and action from movement or input
			Direction newDirection = sprite.getDirection();
			Action newAction = Action.IDLE;

			MotionComponent motion = entity.getComponent(MotionComponent.class);
			if (motion==null) return;
			float dx = motion.getVx();
			float dy = motion.getVy();
			GravityComponent gravity = entity.getComponent(GravityComponent.class);
			if (gravity == null) {
				if (Math.abs(dx) > 0 || Math.abs(dy) > 0) {
					newAction = Action.WALK;
					// Determine direction based on largest absolute velocity or delta
					if (Math.abs(dx) > Math.abs(dy)) {
						newDirection = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
					} else {
						newDirection = (dy > 0) ? Direction.DOWN : Direction.UP;
					}
				} else {
					newAction = Action.IDLE;
				}
			} else {
				if (dy == 0) {
					if (dx!=0) {
						newAction = Action.WALK;
						newDirection = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
					} else {
						newAction = Action.IDLE;
					}
				} else {
					newAction = (dy > 0) ? Action.FALL : Action.JUMP;
					if (dx!=0) {
						newDirection = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
					}
				}
			}

			// Update sprite if direction/action changed
			if (sprite.isLooping() && (sprite.getDirection() != newDirection || sprite.getAction() != newAction)) {
				sprite.setDirection(newDirection);
				sprite.setAction(newAction);
				sprite.setCurrentFrame(0);
				sprite.setElapsedTime(0.0f);
			} else {
				updateSpriteFrame(sprite, deltaTime);
			}
		}

		if (entity.hasComponent(TileMapComponent.class)) {
			TileMapComponent tileMapComponent = entity.getComponent(TileMapComponent.class);
			updateTileMapAnimation(tileMapComponent, deltaTime);
		}
	}

	private void handleThrowAnimation(SpriteComponent sprite, ThrowProjectileComponent throwProjectile) {
	    sprite.setAction(Action.THROW);

	    // Calcola il frame corrente utilizzando il metodo del ThrowProjectileComponent
	    int currentFrame = throwProjectile.getCurrentFrame(sprite.getLength());

	    // Imposta il frame corrente nell'animazione
	    sprite.setCurrentFrame(currentFrame);
	}
	
	private void handleAttackAnimation(SpriteComponent sprite, AttackComponent attack) {
		sprite.setAction(Action.ATTACK);
		int totalFrames = sprite.getLength();
		float animationTimePerFrame = attack.getAttackDuration() / totalFrames;
		int currentFrame = (int) (attack.getAttackTimer() / animationTimePerFrame);
		if (currentFrame >= totalFrames)
			currentFrame = totalFrames - 1; // clamp
		sprite.setCurrentFrame(currentFrame);
	}

	private void updateSpriteFrame(SpriteComponent sprite, float deltaTime) {
		float elapsedTime = sprite.getElapsedTime();
		float frameDuration = sprite.getFrameDuration();
		elapsedTime += deltaTime;

		if (elapsedTime >= frameDuration) {
			int currentFrame = sprite.getCurrentFrame();
			elapsedTime -= frameDuration;

			if (sprite.hasFrame(currentFrame + 1)) {
				sprite.setCurrentFrame(currentFrame + 1);
			} else if (sprite.isLooping()) {
				sprite.setCurrentFrame(0);
			} else {
				// End of animation
				if (sprite.getOnAnimationEnd() != null)
					sprite.getOnAnimationEnd().run();
			}
		}
		sprite.setElapsedTime(elapsedTime);
	}

	private void updateTileMapAnimation(TileMapComponent tileMapComponent, float deltaTime) {
		tileMapComponent.update(deltaTime);
	}
}