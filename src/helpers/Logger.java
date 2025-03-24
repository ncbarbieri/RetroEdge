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