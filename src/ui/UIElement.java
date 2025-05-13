package ui;

import java.awt.Graphics2D;
import engine.components.KeyInputComponent;
import input.MouseInputHandler;

public abstract class UIElement {
    private UIElement parent; 
    protected int x;
    protected int y;
    protected boolean visible;
    private int zIndex;
    
    // Indicates if this element should be offset by camera/view transformations.
    private boolean useCameraOffsets;

    public UIElement(int x, int y, int zIndex) {
        this.x = x;
        this.y = y;
        this.zIndex = zIndex;
        this.visible = false;
        this.useCameraOffsets = false; // By default, no camera offsets.
    }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public int getLocalX() { return x; }
    public int getLocalY() { return y; }

    /**
     * Computes the element’s global X coordinate by accumulating parents' positions.
     */
    public int getGlobalX() {
        int globalX = x;
        if (parent != null) {
            globalX += parent.getGlobalX();
        }
        return globalX;
    }

    /**
     * Computes the element’s global Y coordinate by accumulating parents' positions.
     */
    public int getGlobalY() {
        int globalY = y;
        if (parent != null) {
            globalY += parent.getGlobalY();
        }
        return globalY;
    }

    public int getZIndex() { return zIndex; }
    public void setZIndex(int zIndex) { this.zIndex = zIndex; }

    public boolean isVisible() { return visible; }
    public void show() { this.visible = true; }
    public void hide() { this.visible = false; }

    public void setParent(UIElement parent) { this.parent = parent; }
    public UIElement getParent() { return parent; }

    public boolean usesCameraOffsets() {
        return useCameraOffsets;
    }

    public void setUseCameraOffsets(boolean useCameraOffsets) {
        this.useCameraOffsets = useCameraOffsets;
    }

    public abstract void update(float deltaTime);
    public abstract void render(Graphics2D g, int cameraX, int cameraY);

    public void handleInput(KeyInputComponent keyInput) {}
    public void handleMouseInput(MouseInputHandler mouseInput) {}
}