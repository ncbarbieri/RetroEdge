package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class Label extends UIElement {
    private String text;
    private Font font;
    private Color color;

    public Label(int x, int y, int zIndex, String text, Font font, Color color) {
        super(x, y, zIndex);
        this.text = text;
        this.font = font;
        this.color = color;
        // A label in a static UI (like a menu) doesn't need camera offsets:
        setUseCameraOffsets(false);
    }

    @Override
    public void render(Graphics2D g2d, int cameraX, int cameraY) {
        if (!isVisible()) return;
        g2d.setFont(font);
        g2d.setColor(color);

        int drawX = getGlobalX();
        int drawY = getGlobalY();

        // Only apply offsets if needed
        if (usesCameraOffsets()) {
            drawX -= cameraX;
            drawY -= cameraY;
        }

        g2d.drawString(text, drawX, drawY);
    }

    public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
    public void update(float deltaTime) {
        // No updates needed
    }
}