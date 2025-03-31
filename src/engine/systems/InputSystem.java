/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.systems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import engine.BaseSystem;
import engine.Engine;
import enums.EngineState;
import helpers.Logger;
import input.ActionStateManager;
import input.KeyboardInputHandler;

public class InputSystem extends BaseSystem {
    private final KeyboardInputHandler inputHandler;
//    private final MouseInputHandler mouseInputHandler;

    private final Map<String, Integer> keyBindings; // Action -> KeyCode mapping
    private final Map<String, Consumer<Float>> customKeyActions; // Action -> Custom code
    private final Set<String> debouncedActions;
    private final Set<String> activatedDebouncedActions;

    public InputSystem(Engine engine) {
        super(engine, 1); // High priority for input
        this.inputHandler = engine.getInputHandler();
//        this.mouseInputHandler = engine.getMouseInputHandler();
        this.keyBindings = new HashMap<>();
        this.customKeyActions = new HashMap<>();
        this.debouncedActions = new HashSet<>();
        this.activatedDebouncedActions = new HashSet<>();
    }

    @Override
    protected void initStateUpdateMap() {
        setUpdateInState(EngineState.STARTING, false);
        setUpdateInState(EngineState.RUNNING, true);
        setUpdateInState(EngineState.PAUSED, true);
        setUpdateInState(EngineState.CUTSCENE, true);
        setUpdateInState(EngineState.SHOWING_DIALOG, true);
    	setUpdateInState(EngineState.EXITING, false);
    	setUpdateInState(EngineState.ENTERING, false);
    }

    @Override
    public void update(float deltaTime) {
        // Reset consumed actions for the current frame
        ActionStateManager.resetConsumedActions();

        // Gather pressed and released keys
        Set<Integer> pressedKeys = inputHandler.getPressedKeys();
        Set<Integer> releasedKeys = inputHandler.getReleasedKeys();

        // Build action states from input
        Map<String, Boolean> actionStates = buildActionStates(pressedKeys, releasedKeys);

        // Update global ActionStateManager
        for (Map.Entry<String, Boolean> entry : actionStates.entrySet()) {
            if (entry.getValue()) {
                ActionStateManager.activateAction(entry.getKey());
            } else {
                ActionStateManager.deactivateAction(entry.getKey());
            }
        }

        // Execute custom key actions if any
        executeCustomKeyActions(deltaTime);

        // At this point, actions are updated in ActionStateManager.
        // Consumption should be done by the systems that handle the actions immediately upon detection.

        // KeyInputComponents now query directly from ActionStateManager, so no updating states is required.
        // They will get real-time info when they call isActionActive().

        // Clear released keys for the next frame
        inputHandler.resetReleasedKeys();
    }

    private Map<String, Boolean> buildActionStates(Set<Integer> pressedKeys, Set<Integer> releasedKeys) {
        Map<String, Boolean> newStates = new HashMap<>();

        for (var entry : keyBindings.entrySet()) {
            String action = entry.getKey();
            int keyCode = entry.getValue();

            if (pressedKeys.contains(keyCode)) {
                if (debouncedActions.contains(action)) {
                    if (!activatedDebouncedActions.contains(action)) {
                        newStates.put(action, true);
                        activatedDebouncedActions.add(action);
                    } else {
                        newStates.put(action, false); // Prevent continuous activation
                    }
                } else {
                    newStates.put(action, true); // Regular action
                }
            } else if (releasedKeys.contains(keyCode)) {
                newStates.put(action, false);
                activatedDebouncedActions.remove(action); // Reset debounce on release
            } else {
                newStates.put(action, false);
            }
        }

        return newStates;
    }

    private void executeCustomKeyActions(float deltaTime) {
        for (var entry : customKeyActions.entrySet()) {
            String action = entry.getKey();
            
            // Check if the action is active according to the global action states
            if (ActionStateManager.isActionActive(action)) {
                // Execute the custom action
                customKeyActions.get(action).accept(deltaTime);
                
                // If you want the custom action to be consumed after execution (one-shot), consume it now.
                // This ensures the action won't trigger other systems in the same frame.
//                ActionStateManager.consumeAction(action);
            }
        }
    }
    public void bindAction(String action, int keyCode) {
        keyBindings.put(action, keyCode);
        Logger.log("Bound action '" + action + "' to key " + keyCode);
    }

    public void unbindAction(String action) {
        keyBindings.remove(action);
        customKeyActions.remove(action);
        Logger.log("Unbound action '" + action + "'");
    }

    public void addDebouncedAction(String action) {
        debouncedActions.add(action);
        Logger.log("Added debounced action: " + action);
    }

    public void removeDebouncedAction(String action) {
        debouncedActions.remove(action);
        activatedDebouncedActions.remove(action);
        Logger.log("Removed debounced action: " + action);
    }

    public void bindCustomAction(String action, int keyCode, Consumer<Float> callback) {
        keyBindings.put(action, keyCode);
        customKeyActions.put(action, callback);
        Logger.log("Bound custom action '" + action + "' to key " + keyCode);
    }
}