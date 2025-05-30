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
import ui.UIDialogue;

public class DialogueComponent extends Component {
    private UIDialogue dialogueElement; // Elemento UI per il dialogo
    private KeyInputComponent keyInput;

    public DialogueComponent(Entity entity, UIDialogue dialogueElement, KeyInputComponent keyInput) {
        super(entity);
        this.dialogueElement = dialogueElement;
        this.keyInput = keyInput;
    }

    public KeyInputComponent getKeyInputComponent() {
        return keyInput;
    }

    public UIDialogue getDialogueElement() {
        return dialogueElement;
    }

    public void startDialogue() {
        if (dialogueElement != null) {
            dialogueElement.show();
        }
    }

    public void endDialogue() {
        if (dialogueElement != null) {
            dialogueElement.hide();
        }
    }
}