package transition;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;

public class FadeOutEffect implements TransitionEffect {
    private float alpha; // Opacity
    private float duration;
    private float elapsedTime;

    public FadeOutEffect(float duration) {
        this.alpha = 0.0f; // Start fully visible
        this.duration = duration;
        this.elapsedTime = 0;
    }

    @Override
    public void start() {
        elapsedTime = 0;
//        Logger.log("FadeOutEffect started for " + duration + " seconds.");
    }

    @Override
    public void update(float deltaTime) {
        elapsedTime += deltaTime;
        alpha = Math.min(1.0f, elapsedTime / duration); // Fade out
//        Logger.log("Fade out: alpha="+alpha);
    }

    @Override
    public void render(Graphics2D g) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GamePanel.GAME_WIDTH, GamePanel.GAME_HEIGHT);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    @Override
    public boolean isCompleted() {
        return elapsedTime >= duration;
    }

    @Override
    public float getDuration() {
        return duration;
    }
}