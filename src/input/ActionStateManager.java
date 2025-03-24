package input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActionStateManager {
    private static final Map<String, Boolean> actionStates = new HashMap<>();
    private static final Set<String> consumedActions = new HashSet<>();

    public static void activateAction(String action) {
        if (!isActionActive(action)) {
            actionStates.put(action, true);
        }
    }

    public static void deactivateAction(String action) {
        actionStates.put(action, false);
    }

    public static void consumeAction(String action) {
        if (isActionActive(action)) {
            consumedActions.add(action);
            actionStates.put(action, false); // Ensure immediately inactive
        }
    }

    public static boolean isActionActive(String action) {
        return actionStates.getOrDefault(action, false) && !consumedActions.contains(action);
    }

    public static void resetConsumedActions() {
        consumedActions.clear();
    }

    public static Map<String, Boolean> getCurrentStates() {
        // Returns a snapshot, but note that isActionActive checks consumedActions
        Map<String, Boolean> current = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : actionStates.entrySet()) {
            current.put(entry.getKey(), isActionActive(entry.getKey()));
        }
        return current;
    }
}