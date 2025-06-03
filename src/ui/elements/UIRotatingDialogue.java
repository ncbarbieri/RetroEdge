package ui.elements;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import engine.EngineStateManager;
import engine.components.KeyInputComponent;
import enums.EngineState;
import helpers.Logger;
import input.ActionStateManager;
import main.GamePanel;
import ui.UIDialogue;

public class UIRotatingDialogue extends UIDialogue {
    private final int width;
    private final int height;
    private final BufferedImage panel;
    private BufferedImage[] dialogueImages;
    private final Color textColor;
    private final Color backgroundColor;
    private final Font textFont;
    private final EngineStateManager stateManager;
    private final FontMetrics fontMetrics;
    private int currentMessageIndex;

    public UIRotatingDialogue(int x, int y, int zIndex, EngineStateManager stateManager, Font font, Color textColor, Color backgroundColor, String frameFile) {
        super(x, y, zIndex);
        this.stateManager = stateManager;
        this.textFont = font;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;

        BufferedImage loadedPanel = null;
        int loadedWidth = 0;
        int loadedHeight = 0;
        try (InputStream is = getClass().getResourceAsStream(frameFile)) {
            loadedPanel = ImageIO.read(is);
            loadedWidth = loadedPanel.getWidth();
            loadedHeight = loadedPanel.getHeight();
            setX((GamePanel.GAME_WIDTH - loadedWidth) / 2);
            setY(GamePanel.GAME_HEIGHT - (GamePanel.GAME_HEIGHT / 8) - loadedHeight);
        } catch (IOException e) {
            Logger.log("IOException while loading dialogue panel", e);
        }

        this.panel = loadedPanel;
        this.width = loadedWidth;
        this.height = loadedHeight;

        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tempImg.createGraphics();
        g.setFont(textFont);
        this.fontMetrics = g.getFontMetrics();
        g.dispose();
    }

    public void setDialogues(List<String> dialogues) {
        if (dialogues == null || dialogues.isEmpty()) {
            dialogueImages = null;
            return;
        }
        dialogueImages = new BufferedImage[dialogues.size()];
        for (int i = 0; i < dialogues.size(); i++) {
            dialogueImages[i] = renderTextToImage(dialogues.get(i));
        }
        currentMessageIndex = 0;
    }

    private BufferedImage renderTextToImage(String dialogue) {
        String[] lines = dialogue.split("\n");
        int imgWidth = 0;
        int imgHeight = 0;

        for (String line : lines) {
            imgWidth = Math.max(imgWidth, fontMetrics.stringWidth(line));
            imgHeight += fontMetrics.getHeight();
        }

        BufferedImage textImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = textImage.createGraphics();
        g2d.setFont(textFont);
        g2d.setColor(textColor);

        int yPos = 0;
        for (String line : lines) {
            g2d.drawString(line, 0, yPos + fontMetrics.getAscent());
            yPos += fontMetrics.getHeight();
        }
        g2d.dispose();

        return textImage;
    }

    @Override
    public void startDialogue() {
        if (dialogueImages == null) return;

        if (stateManager.getCurrentState() == EngineState.RUNNING) {
            stateManager.requestStateChange(EngineState.SHOWING_DIALOG);
            setState(DialogueState.ACTIVE);
            this.show();
        }
    }

    @Override
    public void updateDialogue(float deltaTime) {
        // No dynamic updates needed
    }

    @Override
    protected void renderDialogue(Graphics2D g, int xOffset, int yOffset) {
        if (dialogueImages == null || !isVisible()) return;

        int drawX = getGlobalX();
        int drawY = getGlobalY();

        // Only apply offsets if needed
        if (usesCameraOffsets()) {
            drawX -= xOffset;
            drawY -= yOffset;
        }

        g.setColor(backgroundColor);
        g.fillRect(drawX, drawY, width, height);
        g.drawImage(panel, drawX, drawY, null);
        g.drawImage(dialogueImages[currentMessageIndex], drawX + 20, drawY + 20, null);
    }

    @Override
    public void handleInput(KeyInputComponent keyInput) {
        if (state == DialogueState.FINISHED) {
            return;
        }

        if (keyInput.isActionActive("DIALOG")) {
            ActionStateManager.consumeAction("DIALOG");
            setState(DialogueState.FINISHED);
        }
    }

    @Override
    protected void onDialogueFinished() {
        if (stateManager.getCurrentState() == EngineState.SHOWING_DIALOG) {
            stateManager.requestStateChange(EngineState.RUNNING);
            hide();
            currentMessageIndex = (currentMessageIndex + 1) % dialogueImages.length;
        }
    }
}