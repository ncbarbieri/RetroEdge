package state;

import engine.Engine;
import engine.entity.Entity;
import engine.system.BaseSystem;
import helpers.Logger;
import transition.TransitionEffect;

public abstract class GameState {
	protected Engine engine;

	public GameState(Engine engine) {
		this.engine = engine;
		Entity.resetId();
	}
	
    public abstract void init();
    public abstract void cleanup();

    public TransitionEffect getEnterTransition() {
        return null; // Default: nessuna transizione
    }

    public TransitionEffect getExitTransition() {
        return null; // Default: nessuna transizione
    }

    /**
     * Registra un'entità nel motore.
     */
	protected void add(Entity entity) {
	    Logger.log("Adding " + entity.toString());
	    engine.addEntity(entity);
	}
    /**
     * Registra un sistema nel motore.
     */
    protected void add(BaseSystem system) {
        Logger.log("Adding " + system.getClass().getSimpleName());
        engine.addSystem(system);
    }
}
