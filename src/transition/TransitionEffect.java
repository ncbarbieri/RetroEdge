package transition;

import java.awt.Graphics2D;

public interface TransitionEffect {
    float getDuration(); // Time in seconds for the effect
    void start();
    void update(float deltaTime);
    void render(Graphics2D g);
    boolean isCompleted();
}