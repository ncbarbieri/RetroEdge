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

public class TimerComponent extends Component {

	/**
	 * A component that holds a countdown timer. Once the timer
	 * reaches 0, a callback is executed.
	 */
	
    private float timeLeft;       // time remaining in seconds
    private float totalTime; // the original total time
    private Runnable onTimeOver; // code to execute when time is up
    private boolean active;       // whether the timer is active
    private boolean looping;         

    /**
     * @param entity The entity that owns this component
     * @param totalTime total countdown time in seconds
     * @param onTimeOver callback to run when time is over
     */
    public TimerComponent(Entity entity, float totalTime, boolean looping) {
        super(entity);
        this.totalTime = totalTime;
        this.timeLeft = totalTime;
        this.onTimeOver = null;
        this.active = true;
        this.looping = looping;
    }

    /**
     * Decrements the timer by dt seconds.
     * If it reaches 0 or below, executes the callback.
     * @param dt time elapsed in seconds
     */
    public void updateTimer(float dt) {
        if (!active) return;

        timeLeft -= dt;
        if (timeLeft <= 0) {
            // Time is over: run the callback
            if (onTimeOver != null) {
                onTimeOver.run();
            }
            if (looping) {
            	reset();
            } else {
                timeLeft = 0;
                active = false;
            }
        }
    }

    /**
     * Resets the timer to the original total time and makes it active again.
     */
    public void reset() {
        this.timeLeft = totalTime;
        this.active = true;
    }

    public float getTimeLeft() { return timeLeft; }
    public void setOnTimeOver(Runnable onTimeOver) { this.onTimeOver = onTimeOver; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

	public boolean isLooping() { return looping; }
	public void setLooping(boolean looping) { this.looping = looping; }
    
}