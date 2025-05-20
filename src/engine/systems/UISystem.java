package engine.systems;

import java.awt.Graphics2D;
import engine.BaseSystem;
import engine.Engine;
import engine.components.KeyInputComponent;
import engine.components.UIComponent;
import engine.Entity;
import enums.EngineState;
import world.Camera;

public class UISystem extends BaseSystem {
    private Camera camera;
    private int currentXOffset;
    private int currentYOffset;

    public UISystem(Engine engine, Camera camera) {
        super(engine, 11); // Il layer del rendering dell'UI è alto
        this.camera = camera;
    }

    @Override
    protected void initStateUpdateMap() {
        // Customize states where this system updates
    	setUpdateInState(EngineState.STARTING, false);
    	setUpdateInState(EngineState.RUNNING, true);
    	setUpdateInState(EngineState.CUTSCENE, true);
    	setUpdateInState(EngineState.PAUSED, false);
    	setUpdateInState(EngineState.SHOWING_DIALOG, true);
    	setUpdateInState(EngineState.EXITING, false);
    	setUpdateInState(EngineState.ENTERING, false);
    }

    @Override
    public void update(float deltaTime) {
        // Itera in modo sicuro sulle entità con UIComponent
        engine.accessEntitiesWithComponent(UIComponent.class, uiEntities -> {
            for (Entity entity : uiEntities) {
                UIComponent uiComponent = entity.getComponent(UIComponent.class);
                KeyInputComponent keyInput = entity.getComponent(KeyInputComponent.class);

                if (uiComponent != null) {
                    // Gestisce l'input se presente
                    if (keyInput != null) {
                        uiComponent.handleInput(keyInput);
                    }

                    // Aggiorna l'elemento UI
                    uiComponent.getUIElement().update(deltaTime);
                }
            }
        });
    }

    @Override
    public void render(Graphics2D g) {
        // Itera in modo sicuro sulle entità con UIComponent
        engine.accessEntitiesWithComponent(UIComponent.class, uiEntities -> {
            currentXOffset = (camera != null) ? camera.getxOffset() : 0;
            currentYOffset = (camera != null) ? camera.getyOffset() : 0;

            for (Entity entity : uiEntities) {
                UIComponent uiComponent = entity.getComponent(UIComponent.class);
                if (uiComponent != null && uiComponent.getUIElement().isVisible()) {
                    uiComponent.getUIElement().render(g, currentXOffset, currentYOffset);
                }
            }
        });
    }
}