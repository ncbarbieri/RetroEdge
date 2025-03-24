package engine;

import input.InputHandler;
import input.MouseInputHandler;
import state.PlayState;

public class GameEngine extends Engine {

	public GameEngine(InputHandler inputHandler, MouseInputHandler mouseInputHandler) {
		super(inputHandler, mouseInputHandler);
	}

	@Override
	protected void init() {
		this.setNextState(new PlayState(this));
	}

}
