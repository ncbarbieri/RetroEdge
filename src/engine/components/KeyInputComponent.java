/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.components;

import engine.Component;
import engine.Entity;
import input.ActionStateManager;

public class KeyInputComponent extends Component {
	
	private boolean enabled;

    public KeyInputComponent(Entity entity) {
        super(entity);
        enabled = true;
    }

    public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
     * Instead of storing states, just query the ActionStateManager directly.
     */
    public boolean isActionActive(String action) {
        boolean isActive = ActionStateManager.isActionActive(action);
        return enabled && isActive;
    }
}