/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class KeyboardInputHandler implements KeyListener {

    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Set<Integer> releasedKeys = new HashSet<>();

    public KeyboardInputHandler() {
        // Inizializzazione
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        releasedKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        releasedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Non utilizzato
    }

    /**
     * Restituisce se un tasto è attualmente premuto.
     */
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    /**
     * Restituisce se un tasto è stato appena rilasciato.
     */
    public boolean isKeyReleased(int keyCode) {
        return releasedKeys.contains(keyCode);
    }

    /**
     * Cancella lo stato dei tasti rilasciati (per evitare che persistano tra i frame).
     */
    public void resetReleasedKeys() {
        releasedKeys.clear();
    }

    /**
     * Cancella lo stato dei tasti premuti (per evitare che persistano tra i frame).
     */
    public void resetPressedKeys() {
        releasedKeys.clear();
    }

    /**
     * Restituisce un set immutabile di tutti i tasti attualmente premuti.
     *
     * @return Set contenente i codici dei tasti premuti.
     */
	public Set<Integer> getPressedKeys() {
		return Collections.unmodifiableSet(pressedKeys);
	}

    /**
     * Restituisce un set immutabile di tutti i tasti attualmente rilasciati.
     *
     * @return Set contenente i codici dei tasti rilasciati.
     */
	public Set<Integer> getReleasedKeys() {
		return Collections.unmodifiableSet(releasedKeys);
	}

}