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

    private int health;
    private int maxHealth;
    private Runnable onDeath;

    public HealthComponent(Entity entity, int maxHealth) {
        super(entity);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void decreaseHealth(int amount) {
        health -= amount;
        if (health < 0) health = 0;

        if (health == 0 && onDeath != null) {
            onDeath.run();
        }
    }

    public void increaseHealth(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    public void resetHealth() {
        health = maxHealth;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public boolean isDead() { return health <= 0; }

    public void setOnDeath(Runnable onDeath) {
        this.onDeath = onDeath;
    }

    public void kill() {
        health = 0;
        if (onDeath != null) onDeath.run();
    }
}