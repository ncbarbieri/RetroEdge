package ui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import engine.components.KeyInputComponent;
import input.MouseInputHandler;

public class UIGroup extends UIElement {
    private final List<UIElement> children;

    public UIGroup(int x, int y, int zIndex) {
        super(x, y, zIndex);
        this.children = new ArrayList<>();
    }

    public void addChild(UIElement child) {
        if (child != null && child != this) {
            child.setParent(this);
            children.add(child);
            children.sort(Comparator.comparingInt(UIElement::getZIndex));
        }
    }

    public void removeChild(UIElement child) {
        children.remove(child);
        child.setParent(null);
    }

    public void clearChildren() {
        for (UIElement child : children) {
            child.setParent(null);
        }
        children.clear();
    }

    @Override
    public void render(Graphics2D g, int xOffset, int yOffset) {
        if (isVisible()) {
            // Optionally render a background for the group here
            for (UIElement child : children) {
                child.render(g, xOffset, yOffset);
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        if (isVisible()) {
            for (UIElement child : children) {
                child.update(deltaTime);
            }
        }
    }

    @Override
    public void handleInput(KeyInputComponent keyInput) {
        if (isVisible()) {
            for (UIElement child : children) {
                child.handleInput(keyInput);
            }
        }
    }

    @Override
    public void handleMouseInput(MouseInputHandler mouseInput) {
        if (isVisible()) {
            for (UIElement child : children) {
                child.handleMouseInput(mouseInput);
            }
        }
    }
}