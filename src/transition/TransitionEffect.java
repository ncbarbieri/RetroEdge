/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package transition;

import java.awt.Graphics2D;

public interface TransitionEffect {
    float getDuration(); // Time in seconds for the effect
    void start();
    void update(float deltaTime);
    void render(Graphics2D g);
    boolean isCompleted();
}