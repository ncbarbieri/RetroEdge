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
import pathfinder.Node;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class InteractionComponent extends Component {

    private boolean interactable;
    private Consumer<Entity> onEntityInteract; // Usa un Consumer per accettare un parametro
    private Consumer<Set<Node>> onTileInteract; // Usa un Consumer per accettare un parametro
    private Set<Entity> interactionSet; // Usare un Set per le interazioni uniche
    private Set<Node> collisionTiles;   // Nuovo Set per i nodi in collisione

    public InteractionComponent(Entity entity) {
        super(entity);
        interactable = true;
        onEntityInteract = null;
        onTileInteract = null;
        interactionSet = new HashSet<>(); // Inizializza l'HashSet
        collisionTiles = new HashSet<>(); // Inizializza il Set di nodi
    }

    public boolean isInteractable() {
        return interactable;
    }

    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    public void setOnEntityInteract(Consumer<Entity> onEntityInteract) {
        this.onEntityInteract = onEntityInteract; // Imposta l'azione da eseguire durante l'interazione
    }

    public void entityInteract(Entity other) {
        if (onEntityInteract != null) {
        	onEntityInteract.accept(other); // Esegui l'azione con il parametro Entity
        }
    }

    public Set<Entity> getInteractionSet() {
        return interactionSet;
    }

    public void clearInteractions() {
        interactionSet.clear(); // Svuota le interazioni
    }
    
    // Metodi per gestire i nodi in collisione
    public void setOnTileInteract(Consumer<Set<Node>> onTileInteract) {
        this.onTileInteract = onTileInteract; // Imposta l'azione da eseguire durante l'interazione
    }

    public void tileInteract(Set<Node> tiles) {
        if (onTileInteract != null) {
        	onTileInteract.accept(tiles); // Esegui l'azione con il parametro Entity
        }
    }

    public Set<Node> getCollisionTiles() {
        return collisionTiles;
    }

    public void addCollisionTile(Node node) {
        collisionTiles.add(node); // Aggiungi un nodo in collisione
    }

    public void addCollisionTiles(Set<Node> nodes) {
        collisionTiles.addAll(nodes); // Aggiungi più nodi in collisione
    }

    public void clearCollisionTiles() {
        collisionTiles.clear(); // Svuota il set di nodi in collisione
    }
}