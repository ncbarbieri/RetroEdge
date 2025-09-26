/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.systems;

import java.util.List;
import engine.BaseSystem;
import engine.Engine;
import engine.components.ChaseComponent;
import engine.components.ColliderComponent;
import engine.components.MotionComponent;
import engine.Entity;
import enums.EngineState;
import pathfinder.Node;
import pathfinder.PathFinder;
import world.TileMap;

public class ChaseSystem extends BaseSystem {

	public ChaseSystem(Engine engine) {
		super(engine, 3); // Imposta la priorità del sistema
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
	protected void updateEntity(Entity entity, float deltaTime) {
		ChaseComponent chasingComponent = entity.getComponent(ChaseComponent.class);
		if (chasingComponent != null) {
			followTarget(entity, chasingComponent, deltaTime);
		}
	}

	private void followTarget(Entity chaser, ChaseComponent chaseComponent, float deltaTime) {
		MotionComponent chaserMotion = chaser.getComponent(MotionComponent.class);
		ColliderComponent chaserCollider = chaser.getComponent(ColliderComponent.class);

		if (chaserMotion == null || chaserCollider == null) {
			return; // Se mancano componenti necessari, salta l'entità
		}

		Entity target = chaseComponent.getTarget();
		MotionComponent targetMotion = target.getComponent(MotionComponent.class);
		ColliderComponent targetCollider = target.getComponent(ColliderComponent.class);

		if (targetMotion == null || targetCollider == null) {
			return; // Se il bersaglio non ha una posizione o una bounding box, salta l'entità
		}

		PathFinder pathFinder = chaseComponent.getPathfinder();
		TileMap tileManager = chaseComponent.getTileManager();

		// Calcola il centro della bounding box del chaser
		float chaserCenterX = chaserMotion.getX() + chaserCollider.getBoundingBox().x
				+ chaserCollider.getBoundingBox().width / 2f;
		float chaserCenterY = chaserMotion.getY() + chaserCollider.getBoundingBox().y
				+ chaserCollider.getBoundingBox().height / 2f;

		// Calcola il centro della bounding box del target
		float targetCenterX = targetMotion.getX() + targetCollider.getBoundingBox().x
				+ targetCollider.getBoundingBox().width / 2f;
		float targetCenterY = targetMotion.getY() + targetCollider.getBoundingBox().y
				+ targetCollider.getBoundingBox().height / 2f;

		// Converti i centri delle bounding box in coordinate di griglia
		int startCol = toGridPosition(chaserCenterX, tileManager.getTileWidth()); // Colonna = X
		int startRow = toGridPosition(chaserCenterY, tileManager.getTileHeight()); // Riga = Y
		int goalCol = toGridPosition(targetCenterX, tileManager.getTileWidth()); // Colonna = X
		int goalRow = toGridPosition(targetCenterY, tileManager.getTileHeight()); // Riga = Y

		// Calcola il percorso se necessario
		if (pathFinder.setNodes(startRow, startCol, goalRow, goalCol)) {
			pathFinder.search();
		}

		// Segui il percorso
		List<Node> path = pathFinder.getPath();
		if (path == null || path.isEmpty()) {
			chaserMotion.setVx(0);
			chaserMotion.setVy(0);
			return;
		}
		// Ottieni il nodo corrente e il prossimo nodo
		Node currentNode = pathFinder.getStartNode(); // Nodo iniziale
		Node nextStep = path.get(0); // Nodo successivo

		// Controlla se il nodo successivo è stato raggiunto
		if (currentNode.x == nextStep.x && currentNode.y == nextStep.y) {
			path.remove(0); // Rimuovi il nodo raggiunto
			if (path.isEmpty()) {
				chaserMotion.setVx(0);
				chaserMotion.setVy(0);
				return;
			}
			nextStep = path.get(0); // Ottieni il nuovo nodo successivo
		}
		// Calcola la distanza dal target
		float distanceToTarget = (float) Math
				.sqrt(Math.pow(targetCenterX - chaserCenterX, 2) + Math.pow(targetCenterY - chaserCenterY, 2));

		float stopRadius = 50f;
		// Controlla se il chaser è abbastanza vicino al target
		if (distanceToTarget <= stopRadius) {
			chaserMotion.setVx(0);
			chaserMotion.setVy(0);
			return;
		}

		// Calcola la direzione del movimento basata sui nodi
		float dx = nextStep.x - currentNode.x; // Differenza orizzontale
		float dy = nextStep.y - currentNode.y; // Differenza verticale

		// Normalizza la direzione per mantenere una velocità costante
		float magnitude = (float) Math.sqrt(dx * dx + dy * dy);
		if (magnitude > 0) {
			dx /= magnitude;
			dy /= magnitude;
		}

		// Applica la velocità massima
		float maxSpeed = chaserMotion.getMaxSpeed();
		float velocityX = dx * maxSpeed;
		float velocityY = dy * maxSpeed;

		// Aggiorna le velocità
		chaserMotion.setVx(velocityX);
		chaserMotion.setVy(velocityY);

	}
	
	private int toGridPosition(float pixelCoord, int tileSize) {
		return (int) Math.floor(pixelCoord / tileSize);
	}

}