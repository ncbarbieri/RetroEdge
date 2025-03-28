/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package engine;

import input.KeyboardInputHandler;
import input.MouseInputHandler;
import state.PlayState;

public class GameEngine extends Engine {

	public GameEngine(KeyboardInputHandler inputHandler, MouseInputHandler mouseInputHandler) {
		super(inputHandler, mouseInputHandler);
	}

	@Override
	protected void init() {
		this.setNextState(new PlayState(this));
	}

}
