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

public class HealthComponent extends Component {
    private int maxHealth;
    private int currentHealth;

    public HealthComponent(Entity entity, int maxHealth) {
    	super(entity);
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void decreaseHealth(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
    }

    public void increaseHealth(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
}
