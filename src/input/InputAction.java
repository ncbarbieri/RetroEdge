/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package input;

public final class InputAction {
    public static final String MOVE_LEFT = "LEFT";
    public static final String MOVE_RIGHT = "RIGHT";
    public static final String MOVE_UP = "UP";
    public static final String MOVE_DOWN = "DOWN";
    public static final String JUMP = "JUMP";
    public static final String ATTACK = "ATTACK";
    public static final String DIALOG = "DIALOG";
    public static final String START = "START";
    public static final String PAUSE = "PAUSE";
    public static final String DEBUG = "DEBUG";

    private InputAction() {
        // Classe di utility, non istanziabile
    }
}