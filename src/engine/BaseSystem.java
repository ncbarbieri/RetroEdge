/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package engine;

import java.awt.Graphics2D;
import java.util.EnumMap;

import enums.EngineState;

public abstract class BaseSystem {
    protected Engine engine;
    protected int priority;
    private final EnumMap<EngineState, Boolean> stateUpdateMap;
/*
 * Ordine di esecuzione dei sistemi:
 * 
 * 1. InputSystem
 * 2. MotionSystem
 * 3. ChaseSystem
 * 4. CollisionSystem
 * 5. AttackSystem
 * 6. ProjectileSystem
 * 7. InteractionSystem
 * 8. TimerSystem
 * 9. AnimationSystem
 * 10. RenderingSystem
 * 11. UISystem
 */
    public BaseSystem(Engine engine, int priority) {
        this.engine = engine;
        this.priority = priority;
        this.stateUpdateMap = new EnumMap<>(EngineState.class);
        initStateUpdateMap();
    }

    /**
     * Initializes the state update map with default behavior.
     * Override this method in subclasses to customize the behavior.
     */
    protected void initStateUpdateMap() {
        // Default behavior: update in all states
        for (EngineState state : EngineState.values()) {
            stateUpdateMap.put(state, true);
        }
    }
    
    public void update(float deltaTime) {
        engine.accessEntities(entities -> {
            for (Entity entity : entities) {
                updateEntity(entity, deltaTime);
            }
        });
    }

    protected void updateEntity(Entity entity, float deltaTime) { }

    public void render(Graphics2D g) {
        engine.accessEntities(entities -> {
            for (Entity entity : entities) {
                renderEntity(entity, g);
            }
        });
    }

    protected void renderEntity(Entity entity, Graphics2D g) { }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean shouldUpdateInState(EngineState state) {
        return stateUpdateMap.getOrDefault(state, false);
    }

    public void setUpdateInState(EngineState state, boolean shouldUpdate) {
        stateUpdateMap.put(state, shouldUpdate);
    }
}
