/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.components;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import engine.Component;
import engine.Entity;
import ui.NotificationElement;

public class ProximityComponent extends Component {

    private Predicate<Entity> activationFilter; // Condition for triggering
    private boolean isTriggered;
    private Set<Entity> triggeringEntities; // Entities currently triggering proximity
    private float interactionRange;
    private NotificationElement notificationElement;

    public ProximityComponent(Entity entity, float interactionRange) {
        super(entity);
        this.activationFilter = null;
        this.isTriggered = false;
        this.triggeringEntities = new HashSet<>();
        this.interactionRange = interactionRange;
    }

    public ProximityComponent(Entity entity, float interactionRange, Predicate<Entity> activationFilter) {
        super(entity);
        this.activationFilter = activationFilter;
        this.isTriggered = false;
        this.triggeringEntities = new HashSet<>();
        this.interactionRange = interactionRange;
    }

    /**
     * Returns whether the component is currently triggered.
     */
    public boolean isTriggered() {
        return isTriggered;
    }

    /**
     * Sets the triggered state and shows/hides the notification element.
     */
    public void setTriggered(boolean isTriggered) {
        this.isTriggered = isTriggered;
        if (notificationElement != null) {
            if (isTriggered) {
                notificationElement.show();
            } else {
                notificationElement.hide();
                notificationElement.resetAnimation();
            }
        }
    }

    /**
     * Adds an entity to the set of triggering entities and updates the state.
     */
    public void addTriggeringEntity(Entity entity) {
        if (canActivate(entity)) {
            triggeringEntities.add(entity);
            setTriggered(true); // Mark as triggered
        }
    }

    /**
     * Removes an entity from the set of triggering entities and updates the state.
     */
    public void removeTriggeringEntity(Entity entity) {
        triggeringEntities.remove(entity);
        if (triggeringEntities.isEmpty()) {
            setTriggered(false); // No entities triggering, reset state
        }
    }

    /**
     * Clears all triggering entities and resets the state.
     */
    public void clearTriggeringEntities() {
        triggeringEntities.clear();
        setTriggered(false); // Reset state
    }

    /**
     * Returns the set of entities currently triggering this proximity component.
     */
    public Set<Entity> getTriggeringEntities() {
        return new HashSet<>(triggeringEntities);
    }

    /**
     * Checks if another entity satisfies the activation filter.
     */
    public boolean canActivate(Entity other) {
        return activationFilter == null || activationFilter.test(other);
    }

    public float getInteractionRange() {
        return interactionRange;
    }

    public void setInteractionRange(float interactionRange) {
        this.interactionRange = interactionRange;
    }

    public void setNotificationElement(NotificationElement element) {
        this.notificationElement = element;
    }

    public NotificationElement getNotificationElement() {
        return notificationElement;
    }
}