/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package engine.component;

import engine.entity.Entity;

public abstract class Component {
	protected Entity entity;

	public Component(Entity entity) {
		this.entity = entity;
	}

    public Entity getParentEntity() {
        return entity;
    }
}
