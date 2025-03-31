/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.components;

import java.awt.Rectangle;

import engine.Component;
import engine.Entity;
import enums.Direction;

public class AttackComponent extends Component {
    private final Rectangle[] hitBoxes; // Hitboxes for each direction
    private final int damage;
    private boolean attacking;
    private boolean attackApplied;
    private boolean onCooldown;
    private float attackDuration; // Time in seconds for attack animation
    private float attackTimer;    // Tracks attack animation progress
    private float cooldownDuration;
    private float cooldownTimer;  // Tracks cooldown progress
    private boolean canAttackJump; // Allows attack during jumping

    private Runnable onAttack;    // Hook for attack logic

    public AttackComponent(Entity entity, float attackDuration, float cooldownDuration, int damage) {
        super(entity);
        this.hitBoxes = new Rectangle[Direction.values().length];
        this.attackDuration = attackDuration;
        this.cooldownDuration = cooldownDuration;
        this.damage = damage;
        resetTimers();
    }

    /** Set the hitbox for a specific direction. */
    public void setHitBox(Direction direction, Rectangle hitBox) {
        hitBoxes[direction.getDirectionIndex()] = hitBox;
    }

    /** Hook for custom attack logic. */
    public void setOnAttack(Runnable onAttack) {
        this.onAttack = onAttack;
    }

    public Rectangle getHitBox(int direction) {
    	if (direction>=0 && direction<=hitBoxes.length && hitBoxes[direction]!=null)
    		return (Rectangle) hitBoxes[direction].clone();
    	else
    		return null;
    }

    /** Check if the entity is currently attacking. */
    public boolean isAttacking() {
        return attacking;
    }

    /** Check if the attack cooldown has completed. */
    public boolean canAttack() {
        return !attacking && !onCooldown;
    }

    /** Start the attack animation and logic. */
    public void startAttack() {
        if (canAttack()) {
            attacking = true;
            attackTimer = 0;
            attackApplied = false;
            if (onAttack != null) onAttack.run();
        }
    }

    /** Update attack animation and check for completion. */
    public void updateAttack(float deltaTime) {
        if (!attacking) return;

        attackTimer += deltaTime;
        if (isAttackComplete()) {
            stopAttack();
        }
    }

    /** Check if the attack animation is complete. */
    public boolean isAttackComplete() {
        return attackTimer >= attackDuration;
    }

    /** Stop the attack and trigger cooldown. */
    public void stopAttack() {
        attacking = false;
        onCooldown = true;
        cooldownTimer = cooldownDuration;
    }

    /** Update cooldown timer and reset state if cooldown ends. */
    public void updateCooldown(float deltaTime) {
        if (!onCooldown) return;

        cooldownTimer -= deltaTime;
        if (cooldownTimer <= 0) {
            onCooldown = false;
        }
    }

    /** Check if the attack has been applied (e.g., damage dealt). */
    public boolean isAttackApplied() {
        return attackApplied;
    }

    /** Mark the attack as applied. */
    public void setAttackApplied(boolean applied) {
        this.attackApplied = applied;
    }

    /** Check if attacking during jumping is allowed. */
    public boolean canAttackJump() {
        return canAttackJump;
    }

    /** Enable/disable attacking during jumping. */
    public void setCanAttackJump(boolean canAttackJump) {
        this.canAttackJump = canAttackJump;
    }

    /** Reset all attack and cooldown timers. */
    private void resetTimers() {
        this.attacking = false;
        this.onCooldown = false;
        this.attackTimer = 0;
        this.cooldownTimer = 0;
        this.attackApplied = false;
    }

	public int getDamage() {
		return damage;
	}
	public float getAttackDuration() {
		return attackDuration;
	}

	public float getAttackTimer() {
		return attackTimer;
	}
    
}