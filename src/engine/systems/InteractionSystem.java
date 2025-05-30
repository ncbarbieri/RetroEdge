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
import engine.components.ColliderComponent;
import engine.components.DialogueComponent;
import engine.components.InteractionComponent;
import engine.components.KeyInputComponent;
import engine.components.MotionComponent;
import engine.components.ProximityComponent;
import engine.Entity;
import enums.EngineState;
import input.ActionStateManager;
import pathfinder.Node;
import ui.UIDialogue;
import java.util.Set;

public class InteractionSystem extends BaseSystem {

    public InteractionSystem(Engine engine) {
        super(engine, 7); // System priority
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
        // Process entities with InteractableComponent
        engine.accessEntitiesWithComponent(InteractionComponent.class, interactableEntities -> {
            for (Entity entity : interactableEntities) {
            	handleInteraction(entity, deltaTime);
            }
        });

        engine.accessEntities(entities -> {
            int size = entities.size();
            for (int i = 0; i < size; i++) {
                Entity e1 = entities.get(i);
                ProximityComponent proximity = e1.getComponent(ProximityComponent.class);
                if (proximity != null) {
                    for (int j = 0; j < size; j++) { // Confronta tutte le entità
                        if (i != j && proximity.canActivate(entities.get(j))) {
                            updateEntityPair(e1, entities.get(j), deltaTime);
                        }
                    }
                }
            }
        });
    }

    private void handleInteraction(Entity entity, float deltaTime) {
        InteractionComponent interactable = entity.getComponent(InteractionComponent.class);
        if (interactable == null) {
            return; // L'entità non è interagibile
        }

        Set<Entity> entityInteractionSet = interactable.getInteractionSet();

        // Itera sulle entità nella lista di interazione
        for (Entity interactor : entityInteractionSet) { 
            interactable.entityInteract(interactor); // Gestisce l'interazione
        }

        // Svuota la lista dopo l'elaborazione
        interactable.clearInteractions();

        Set<Node> tileInteractionSet = interactable.getCollisionTiles();
        if (!tileInteractionSet.isEmpty()) {
        	interactable.tileInteract(tileInteractionSet);
            // Svuota la lista dopo l'elaborazione
            interactable.clearCollisionTiles();
        }
	}

    protected void updateEntityPair(Entity entityA, Entity entityB, float deltaTime) {
    	MotionComponent aPosition = entityA.getComponent(MotionComponent.class);
        ColliderComponent aCollider = entityA.getComponent(ColliderComponent.class);
        MotionComponent bPosition = entityB.getComponent(MotionComponent.class);
        ColliderComponent bCollider = entityB.getComponent(ColliderComponent.class);
        ProximityComponent trigger = entityA.getComponent(ProximityComponent.class);

        if (aPosition != null && aCollider != null &&
            bPosition != null && bCollider != null && trigger != null) {

            float interactionRange = trigger.getInteractionRange();

            Vector2 aCenter = calculateBoundingBoxCenter(aPosition, aCollider);
            Vector2 bCenter = calculateBoundingBoxCenter(bPosition, bCollider);

            // Check if entityB is within interaction range of entityA
            if (aCenter.distanceSquared(bCenter) <= interactionRange * interactionRange) {
                // Add entityB as a triggering entity
                trigger.addTriggeringEntity(entityB);
            } else {
                // Remove entityB from the triggering set
                trigger.removeTriggeringEntity(entityB);
            }
            
            Set<Entity> triggeringEntities = trigger.getTriggeringEntities();

            // Handle notification visibility based on triggering state
            if (!triggeringEntities.isEmpty()) {
                trigger.setTriggered(true);
                // Call handleDialogue if the entity has a DialogueComponent
                DialogueComponent dialogue = entityA.getComponent(DialogueComponent.class);
                if (dialogue != null) {
                    handleDialogue(dialogue, entityA, entityB);
                }
            } else {
                trigger.setTriggered(false);
            }
        }
    }
    
    /**
     * Handles dialogue interactions.
     */
    private void handleDialogue(DialogueComponent dialogue, Entity entityA, Entity entityB) {
        // Get the DialogueElement from the DialogueComponent
        UIDialogue dialogueElement = dialogue.getDialogueElement();
        if (dialogueElement == null) {
            return; // No DialogueElement available
        }

        // Check for the triggering entity's KeyInputComponent
        KeyInputComponent keyInput = entityB.getComponent(KeyInputComponent.class);
        if (keyInput == null) {
            return; // KeyInputComponent is required for the player entity
        }

        // Check if the "DIALOG" action is just activated
        if (!keyInput.isActionActive("DIALOG")) {
            return; // The player hasn't triggered the interaction
        }

        // Start or continue the dialogue
        if (dialogueElement.getState() == UIDialogue.DialogueState.FINISHED) {
            ActionStateManager.consumeAction("DIALOG");
            dialogueElement.startDialogue();
        }
    }    
    private Vector2 calculateBoundingBoxCenter(MotionComponent position, ColliderComponent collider) {
        float centerX = position.getX() + collider.getBoundingBox().x + collider.getBoundingBox().width / 2f;
        float centerY = position.getY() + collider.getBoundingBox().y + collider.getBoundingBox().height / 2f;
        return new Vector2(centerX, centerY);
    }

    /**
     * Classe privata per rappresentare un vettore 2D.
     */
    private static class Vector2 {
        private final float x;
        private final float y;

        public Vector2(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float distanceSquared(Vector2 other) {
            float dx = this.x - other.x;
            float dy = this.y - other.y;
            return dx * dx + dy * dy;
        }

        @Override
        public String toString() {
            return "Vector2(" + x + ", " + y + ")";
        }
    }
}