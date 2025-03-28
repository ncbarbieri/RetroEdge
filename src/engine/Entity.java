/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package engine;

import java.util.Map;

import enums.EntityType;
import java.util.HashMap;

public class Entity {
    private int id;
    private static int idCount = 1;
    private EntityType type;
    private int layer; // Priorità di rendering o di gestione
    private boolean alive;
	private Map<Class<? extends Component>, Component> components;

	public Entity(EntityType type, int layer) {
		this.id = idCount++;
		this.type = type;
		this.layer = layer;
		components = new HashMap<>();
		alive = true;
	}

	public <T extends Component> void addComponent(T component) {
	    if (components.containsKey(component.getClass())) {
	        throw new IllegalArgumentException(
	            "Component of type " + component.getClass().getSimpleName() + " already exists in Entity " + id
	        );
	    }
	    components.put(component.getClass(), component);
	}

	public <T extends Component> boolean hasComponent(Class<T> componentClass) {
		return components.containsKey(componentClass);
	}

	public <T extends Component> T getComponent(Class<T> componentClass) {
		return componentClass.cast(components.get(componentClass));
	}
	
    public Map<Class<? extends Component>, Component> getAllComponents() {
        return new HashMap<>(components); // Restituisce una copia per sicurezza
    }

    public <T extends Component> T removeComponent(Class<T> componentClass) {
		return componentClass.cast(components.remove(componentClass));
	}

	public int getId() {
		return id;
	}
	
    public EntityType getType() {
        return this.type;
    }
    
	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
    
    public static void resetId() {
    	idCount = 1;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Entity {id=%d, type=%s, layer=%d, alive=%s, components=%s}",
            id, type, layer, alive, 
            components.keySet().stream()
                     .map(Class::getSimpleName) // Ottiene solo il nome semplice della classe
                     .toList()                 // Converte il risultato in una lista
        );
    }

}
