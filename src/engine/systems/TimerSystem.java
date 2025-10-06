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
import engine.components.TimerComponent;
import engine.Entity;
import enums.EngineState;

public class TimerSystem extends BaseSystem {

	/**
	 * A system that updates all TimerComponents each frame.
	 * Once a timer hits 0, it executes the onTimeOver callback.
	 */
	
    public TimerSystem(Engine engine) {
        super(engine, 8); // priority, adjust as you wish
    }

    @Override
    protected void initStateUpdateMap() {
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
        TimerComponent timer = entity.getComponent(TimerComponent.class);
        if (timer != null && timer.isActive()) {
            timer.updateTimer(deltaTime);
        }
	}
	
}