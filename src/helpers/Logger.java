/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package helpers;

public class Logger {

    // Flag per abilitare/disabilitare il logging
    private static final boolean IS_DEVELOPMENT = true;

    // Metodo per loggare un messaggio semplice
    public static void log(String message) {
        if (IS_DEVELOPMENT) {
            System.out.println("[LOG] " + message);
        }
    }

    // Metodo per loggare un messaggio con eccezione
    public static void log(String message, Exception e) {
        if (IS_DEVELOPMENT) {
            System.out.println("[LOG] " + message);
            if (e != null) {
                e.printStackTrace(System.out);
            }
        }
    }
}