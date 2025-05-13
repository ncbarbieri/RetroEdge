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

public class PlayerComponent extends Component {
	private int gems;

	public PlayerComponent(Entity entity) {
		super(entity);
		this.gems = 0;
	}

	public int getGems() {
		return gems;
	}

	public void addGems(int gems) {
		this.gems += gems;
	}
	
}
