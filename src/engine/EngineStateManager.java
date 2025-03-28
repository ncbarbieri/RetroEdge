/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import enums.EngineState;
import helpers.Logger;

public class EngineStateManager {
    private EngineState currentState;
    private final Map<EngineState, Set<EngineState>> validTransitions;

    public EngineStateManager(EngineState initialState) {
        this.currentState = initialState;
        this.validTransitions = new HashMap<>();
        initializeValidTransitions();
    }

    private void initializeValidTransitions() {
        validTransitions.put(EngineState.STARTING, Set.of(EngineState.ENTERING, EngineState.RUNNING)); // Aggiunto RUNNING
        validTransitions.put(EngineState.ENTERING, Set.of(EngineState.RUNNING));
        validTransitions.put(EngineState.RUNNING, Set.of(EngineState.PAUSED, EngineState.SHOWING_DIALOG, EngineState.CUTSCENE, EngineState.EXITING));
        validTransitions.put(EngineState.PAUSED, Set.of(EngineState.RUNNING));
        validTransitions.put(EngineState.EXITING, Set.of(EngineState.ENTERING));
        validTransitions.put(EngineState.CUTSCENE, Set.of(EngineState.RUNNING));
        validTransitions.put(EngineState.SHOWING_DIALOG, Set.of(EngineState.RUNNING));
    }
    
    public synchronized void requestStateChange(EngineState newState) {
        if (canTransitionTo(newState)) {
            Logger.log(String.format("Switching engine state: %s -> %s", currentState, newState));
        	
        	currentState = newState;
        } else {
            Logger.log("Invalid transition from " + currentState + " to " + newState);
        }
    }

    private boolean canTransitionTo(EngineState newState) {
        return validTransitions.getOrDefault(currentState, Set.of()).contains(newState);
    }

    public EngineState getCurrentState() {
        return currentState;
    }
}