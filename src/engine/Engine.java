package engine;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import engine.component.Component;
import engine.entity.Entity;
import engine.system.BaseSystem;
import enums.EngineState;
import helpers.Logger;
import input.InputHandler;
import input.MouseInputHandler;
import state.GameState;
import transition.TransitionEffect;

public abstract class Engine {
    private final EngineStateManager stateManager;
    private GameState currentState, nextState;
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> entitiesToRemove = new ArrayList<>();
    private final List<Entity> entitiesToAdd = new ArrayList<>();
    private final List<BaseSystem> systems = new ArrayList<>();
    private final Comparator<BaseSystem> systemComparator;
    private final InputHandler inputHandler;
    private final MouseInputHandler mouseInputHandler;
    private TransitionEffect transitionEffect;
    private boolean debug = false;

    public Engine(InputHandler inputHandler, MouseInputHandler mouseInputHandler) {
        this.inputHandler = inputHandler;
        this.mouseInputHandler = mouseInputHandler;
        this.systemComparator = Comparator.comparingInt(BaseSystem::getPriority);
        this.stateManager = new EngineStateManager(EngineState.STARTING); // Stato iniziale valido
        init();
    }

    protected abstract void init();

    public void update(float deltaTime) {

        // If we have a nextState but no transitions in progress and either no currentState or we are allowed to switch:
        if (nextState != null && transitionEffect == null && 
            (currentState == null || stateManager.getCurrentState() == EngineState.RUNNING)) {
            // Trigger state switching logic
            switchState();
            return; // Wait until next frame to proceed
        }

        if (transitionEffect != null) {
        	handleTransition(deltaTime);
        }

        // If we reach here and no transitions are in progress:
        EngineState currentEngineState = stateManager.getCurrentState();

        synchronized (systems) {
            systems.stream()
                .filter(system -> system.shouldUpdateInState(currentEngineState))
                .forEach(system -> system.update(deltaTime));
        }
        
        // Add new entities
        synchronized (entities) {
            if (!entitiesToAdd.isEmpty()) {
//                Logger.log("Adding new entities: " + entitiesToAdd.size());
                entities.addAll(entitiesToAdd);
                entitiesToAdd.clear();
            }
        }

        // Remove dead entities
        removeDeadEntities();
    }
    
    private void switchState() {
        Logger.log(String.format(
            "Switching game state: %s -> %s",
            currentState != null ? currentState.getClass().getSimpleName() : "None",
            nextState != null ? nextState.getClass().getSimpleName() : "None"
        ));

        if (nextState == null) {
//            Logger.log("No nextState is set. Nothing to do in switchState().");
            return;
        }

        if (currentState == null) {
            // Case (a): Engine just started, no current state yet.
//            Logger.log("No current state, engine just started. Switching directly to nextState.");
            currentState = nextState;
            nextState = null;

            // Clear and init the new state
            synchronized (entities) { entities.clear(); }
            synchronized (systems) { systems.clear(); }

            currentState.init();
            
            // If the new state has an enter transition, start it
            TransitionEffect enterEffect = currentState.getEnterTransition();
            if (enterEffect != null) {
//                Logger.log("Starting enter transition: " + enterEffect.getClass().getSimpleName());
                transitionEffect = enterEffect;
                transitionEffect.start();
                stateManager.requestStateChange(EngineState.ENTERING);
            } else {
                // No enter transition, go directly to RUNNING
                stateManager.requestStateChange(EngineState.RUNNING);
                synchronized (systems) {
                    systems.sort(systemComparator);
                }
//                Logger.log("State initialized and running immediately.");
            }
        } else {
            // Case (b): There is a current state running. First handle its exit transition if any.
//            Logger.log("Current state is not null, preparing to exit current state.");

            TransitionEffect exitEffect = currentState.getExitTransition();
            if (exitEffect != null) {
                // Start the exit transition
//                Logger.log("Starting exit transition: " + exitEffect.getClass().getSimpleName());
                transitionEffect = exitEffect;
                transitionEffect.start();
                stateManager.requestStateChange(EngineState.EXITING);
            } else {
                // No exit transition, clean up immediately and move on
//                Logger.log("No exit transition. Cleaning up current state and switching now.");
                currentState.cleanup();

                currentState = nextState;
                nextState = null;

                synchronized (entities) { entities.clear(); }
                synchronized (systems) { systems.clear(); }

                currentState.init();
                TransitionEffect enterEffect = currentState.getEnterTransition();
                if (enterEffect != null) {
//                    Logger.log("Starting enter transition: " + enterEffect.getClass().getSimpleName());
                    transitionEffect = enterEffect;
                    transitionEffect.start();
                    stateManager.requestStateChange(EngineState.ENTERING);
                } else {
                    stateManager.requestStateChange(EngineState.RUNNING);
                    synchronized (systems) {
                        systems.sort(systemComparator);
                    }
//                    Logger.log("State switched, initialized, and running immediately (no enter transition).");
                }
            }
        }
    }
    
    private void handleTransition(float deltaTime) {
        // Update the transition effect
        transitionEffect.update(deltaTime);
        if (transitionEffect.isCompleted()) {
//            Logger.log("Transition effect completed.");
            transitionEffect = null;

            // Handle post-transition logic
            if (stateManager.getCurrentState() == EngineState.EXITING) {
                // Just finished exit transition: cleanup old state, load new one
//                Logger.log("Exit transition finished. Cleaning up old state and switching to next state.");
                if (currentState != null) {
                    currentState.cleanup();
                }

                currentState = nextState;
                nextState = null;

                synchronized (entities) { entities.clear(); }
                synchronized (systems) { systems.clear(); }

                currentState.init();
                TransitionEffect enterEffect = currentState.getEnterTransition();
                if (enterEffect != null) {
//                    Logger.log("Starting enter transition: " + enterEffect.getClass().getSimpleName());
                    transitionEffect = enterEffect;
                    transitionEffect.start();
                    stateManager.requestStateChange(EngineState.ENTERING);
                } else {
                    stateManager.requestStateChange(EngineState.RUNNING);
                    synchronized (systems) {
                        systems.sort(systemComparator);
                    }
//                    Logger.log("State running immediately after exit transition (no enter transition).");
                }

            } else if (stateManager.getCurrentState() == EngineState.ENTERING) {
                // Just finished enter transition: move to RUNNING
//                Logger.log("Enter transition finished. Switching to RUNNING.");
                stateManager.requestStateChange(EngineState.RUNNING);
                synchronized (systems) {
                    systems.sort(systemComparator);
                }
            }
        }
//        return; // Wait until next update call
    	
    }
    
    private void removeDeadEntities() {
        synchronized (entities) {
            entitiesToRemove.clear();
            for (Entity entity : entities) {
                if (!entity.isAlive()) {
                    entitiesToRemove.add(entity);
                }
            }
            entities.removeAll(entitiesToRemove);
        }
    }

    public void render(Graphics2D g) {
        synchronized (systems) {
            systems.forEach(system -> system.render(g));
        }
       
        if (transitionEffect != null) {
        	transitionEffect.render(g);
        }
   }

    public void addEntity(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }

        synchronized (entities) {
            entitiesToAdd.add(entity);
        }
    }

    public void addSystem(BaseSystem system) {
        if (system == null) {
            throw new IllegalArgumentException("System cannot be null.");
        }

        synchronized (systems) {
            systems.add(system);
        }
    }

    public void setNextState(GameState nextState) {
        this.nextState = nextState;
    }

    public EngineStateManager getStateManager() {
        return stateManager;
    }

    /**
     * Sort entities using a given comparator.
     */
    public void sortEntities(Comparator<Entity> comparator) {
        synchronized (entities) {
            entities.sort(comparator);
        }
    }

    /**
     * Thread-safe access to entities via a consumer action.
     */
    public void accessEntities(Consumer<List<Entity>> action) {
        synchronized (entities) {
            action.accept(entities);
        }
    }

    /**
     * Thread-safe access to entities with a specific component via a consumer action.
     */
    public <T extends Component> void accessEntitiesWithComponent(Class<T> componentClass, Consumer<List<Entity>> action) {
        synchronized (entities) {
            List<Entity> filteredEntities = entities.stream()
                .filter(entity -> entity.hasComponent(componentClass))
                .collect(Collectors.toList());
            action.accept(filteredEntities);
        }
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public MouseInputHandler getMouseInputHandler() {
        return mouseInputHandler;
    }

    public void cleanup() {
        try {
            synchronized (entities) {
                synchronized (systems) {
                    if (currentState != null) {
                        currentState.cleanup();
                    }
                    entities.clear();
                    entitiesToAdd.clear();
                    entitiesToRemove.clear();
                    systems.clear();
                }
            }
            Logger.log("Engine cleaned up successfully.");
        } catch (Exception e) {
            Logger.log("Error during cleanup: " + e.getMessage(), e);
        }
    }

	public void windowFocusLost() {
		// we should reset input
//		setPaused(true);
	}

	public void windowGainedFocus() {
//		setPaused(false);
	}

    public GameState getGameState() {
        return currentState;
    }
    
	public boolean isDebug() {
		return debug;
	}
	
    public void setDebug(boolean debug) {
		this.debug = debug;
    	Logger.log("Debug is "+(debug?"ON":"OFF"));
	}

	public String getDebugInfo() {
        return String.format(
            "Entities: %d | Systems: %d | State: %s - %s",
            entities.size(),
            systems.size(),
            currentState != null ? currentState.getClass().getSimpleName() : "None",
            stateManager.getCurrentState()
        );
    }
}