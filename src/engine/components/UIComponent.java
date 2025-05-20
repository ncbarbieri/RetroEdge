package engine.components;

import engine.Component;
import engine.Entity;
import ui.UIElement;

public class UIComponent extends Component {
    private UIElement uiElement;

    public UIComponent(Entity entity, UIElement uiElement) {
        super(entity);
        this.uiElement = uiElement;
    }

    public UIElement getUIElement() {
        return uiElement;
    }

    public void setUIElement(UIElement uiElement) {
        this.uiElement = uiElement;
    }

    public void handleInput(KeyInputComponent keyInput) {
        if (uiElement != null) {
            uiElement.handleInput(keyInput);
        }
    }
}