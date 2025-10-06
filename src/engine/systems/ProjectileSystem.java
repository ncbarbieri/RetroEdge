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
import engine.components.ColliderComponent;
import engine.components.MotionComponent;
import engine.components.SpriteComponent;
import engine.components.ThrowProjectileComponent;
import engine.Entity;
import enums.Direction;
import enums.EngineState;

public class ProjectileSystem extends BaseSystem {

    public ProjectileSystem(Engine engine) {
        super(engine, 6); // Priorità del sistema
    }

    @Override
    protected void initStateUpdateMap() {
        // Stati in cui il sistema deve aggiornarsi
        setUpdateInState(EngineState.STARTING, false);
        setUpdateInState(EngineState.RUNNING, true);
        setUpdateInState(EngineState.CUTSCENE, false);
        setUpdateInState(EngineState.PAUSED, false);
        setUpdateInState(EngineState.SHOWING_DIALOG, false);
        setUpdateInState(EngineState.EXITING, false);
        setUpdateInState(EngineState.ENTERING, false);
    }

    @Override
    protected void updateEntity(Entity entity, float deltaTime) {
        ThrowProjectileComponent throwComp = entity.getComponent(ThrowProjectileComponent.class);
        if (throwComp != null) {
            processThrower(entity, throwComp, deltaTime);
        }
    }

    /**
     * Gestisce il lancio del proiettile.
     *
     * @param thrower   Entità che sta lanciando il proiettile
     * @param throwComp Componente che gestisce i lanci
     * @param deltaTime Tempo trascorso dall'ultimo aggiornamento
     */
    private void processThrower(Entity thrower, ThrowProjectileComponent throwComp, float deltaTime) {
        // Aggiorna il timer del lancio
        throwComp.update(deltaTime);

        // Configura e attiva il proiettile durante il lancio
        if (throwComp.isThrowing() && !throwComp.isProjectileLaunched()) {
            Entity projectile = throwComp.getProjectile();
            if (projectile != null && !projectile.isAlive()) {
            	throwProjectile(thrower, projectile);
                throwComp.setProjectileLaunched(true); // Imposta il proiettile come lanciato
            }
        }
    }

    /**
     * Configura e posiziona il proiettile per il lancio.
     *
     * @param thrower     Entità che lancia il proiettile
     * @param projectile  Proiettile da configurare
     */
	private void throwProjectile(Entity thrower, Entity projectile) {
		MotionComponent throwerMotion = thrower.getComponent(MotionComponent.class);
		MotionComponent projectileMotion = projectile.getComponent(MotionComponent.class);
		SpriteComponent throwerSprite = thrower.getComponent(SpriteComponent.class);
		ColliderComponent throwerCollider = thrower.getComponent(ColliderComponent.class);
		ColliderComponent projectileCollider = projectile.getComponent(ColliderComponent.class);
		Rectangle throwerBox = throwerCollider.getBoundingBox();
		Rectangle projectileBox;
		int dx = 0, dy = 0;
		if (throwerSprite.getDirection() == Direction.LEFT) {
			projectileMotion.setVx(-projectileMotion.getMaxSpeed());
			projectileMotion.setVy(0);
			projectileCollider.setDefaultBoundingBox(Direction.LEFT);
			projectileBox = projectileCollider.getBoundingBox();
			dx = throwerBox.x - projectileBox.x - projectileBox.width - 1;
		} else if (throwerSprite.getDirection() == Direction.RIGHT) {
			projectileMotion.setVx(projectileMotion.getMaxSpeed());
			projectileMotion.setVy(0);
			projectileCollider.setDefaultBoundingBox(Direction.RIGHT);
			projectileBox = projectileCollider.getBoundingBox();
			dx = throwerBox.x + throwerBox.width - projectileBox.x + 1;
		} else if (throwerSprite.getDirection() == Direction.UP) {
			projectileMotion.setVx(0);
			projectileMotion.setVy(-projectileMotion.getMaxSpeed());
			projectileCollider.setDefaultBoundingBox(Direction.UP);
			projectileBox = projectileCollider.getBoundingBox();
			dy = throwerBox.y - projectileBox.y - projectileBox.height - 1;
		} else if (throwerSprite.getDirection() == Direction.DOWN) {
			projectileMotion.setVx(0);
			projectileMotion.setVy(projectileMotion.getMaxSpeed());
			projectileCollider.setDefaultBoundingBox(Direction.DOWN);
			projectileBox = projectileCollider.getBoundingBox();
			dy = throwerBox.y + throwerBox.height - projectileBox.y + 1;
		}
		projectileMotion.setX(throwerMotion.getX() + dx);
		projectileMotion.setY(throwerMotion.getY() + dy);
		projectile.setAlive(true);
		this.engine.addEntity(projectile);
	}
}