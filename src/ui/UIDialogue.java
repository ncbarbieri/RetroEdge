/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package ui;

import java.awt.Graphics2D;
import engine.components.KeyInputComponent;

public abstract class UIDialogue extends UIElement {

	public enum DialogueState {
        ACTIVE,     // Dialogo in corso
        FINISHED    // Dialogo terminato
    }

    protected DialogueState state;
    protected boolean triggered;

    public UIDialogue(int x, int y, int layer) {
        super(x, y, layer);
        this.state = DialogueState.FINISHED; // Stato iniziale
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }
    
    /**
     * Aggiorna lo stato generale.
     */
    @Override
    public void update(float deltaTime) {
        if (state == DialogueState.ACTIVE) {
            updateDialogue(deltaTime);
        }
    }

    /**
     * Disegna il dialogo. Implementazione lasciata alle sottoclassi.
     */
    @Override
    public void render(Graphics2D g, int xOffset, int yOffset) {
        if (state == DialogueState.ACTIVE) {
            renderDialogue(g, xOffset, yOffset);
        }
    }

    /**
     * Inizia il dialogo. Da implementare nelle sottoclassi.
     */
    public abstract void startDialogue();

    /**
     * Aggiorna lo stato del dialogo. Deve essere implementato nelle sottoclassi
     * per definire la logica di avanzamento.
     */
    public abstract void updateDialogue(float deltaTime);

    /**
     * Esegue il render del dialogo.
     */
    protected abstract void renderDialogue(Graphics2D g, int xOffset, int yOffset);

	/**
     * Gestisce l'input del dialogo.
     */
    public abstract void handleInput(KeyInputComponent keyInput);

    /**
     * Cambia lo stato del dialogo.
     */
    protected void setState(DialogueState newState) {
        this.state = newState;
        if (newState == DialogueState.FINISHED) {
            onDialogueFinished();
        }
    }

    public DialogueState getState() {
        return state;
    }

    /**
     * Permette alle sottoclassi di reagire alla fine del dialogo.
     */
    protected void onDialogueFinished() {
    }
}