package engine.components;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import engine.Component;
import engine.Entity;
import enums.CollisionBehavior;
import enums.Direction;
import world.CharacterSpritesheet;

public class ColliderComponent extends Component {
    private Rectangle defaultBoundingBox;
    private Map<Direction, Rectangle> directionalBoundingBoxes; // Bounding box per direzione
    private Rectangle boundingBox;
    private CollisionBehavior behavior;
    private float mass = 1f; // default mass
    private boolean oneWayPlatform = false; // If true and STATIC, it's a platform you can pass through
    private boolean colliding = false; // If true and STATIC, it's a platform you can pass through
    private float timeRemaining = 0f;
    public static final float COLLISION_VISUAL_DURATION = 0.5f; // Durata in secondi

    public ColliderComponent(Entity entity, int x, int y, int width, int height, CollisionBehavior behavior) {
		super(entity);
        this.behavior = behavior;
        this.defaultBoundingBox = new Rectangle(x, y, width, height);
        this.boundingBox = this.defaultBoundingBox;
        this.directionalBoundingBoxes = new HashMap<>();
    }
    
    public ColliderComponent(Entity entity, Rectangle boundingBox, CollisionBehavior behavior) {
		super(entity);
        this.behavior = behavior;
        this.defaultBoundingBox = boundingBox;
        this.boundingBox = boundingBox;
        this.directionalBoundingBoxes = new HashMap<>();
    }
    
    public ColliderComponent(Entity entity, CharacterSpritesheet spritesheet, CollisionBehavior behavior) {
		super(entity);
        this.behavior = behavior;
        this.defaultBoundingBox = boundingBox;
        this.boundingBox = spritesheet.getBoundingBox();
        this.directionalBoundingBoxes = spritesheet.getDirectionalBoundingBoxes();
    }
    
    public void collides() {
    	colliding = true;
    	timeRemaining = COLLISION_VISUAL_DURATION;
    }
    
    public void update(float deltaTime) {
    	timeRemaining -= deltaTime;
        if (timeRemaining <= 0) {
        	colliding = false;
        	timeRemaining = 0f;
        }
    }
    
    public void setDefaultBoundingBox(Direction direction) {
        Rectangle newBoundingBox = directionalBoundingBoxes.get(direction);
        this.boundingBox = (newBoundingBox != null) ? (Rectangle) newBoundingBox.clone() : (Rectangle) defaultBoundingBox.clone();
     }

    public void setBoundingBox(Direction direction, Rectangle boundingBox) {
        directionalBoundingBoxes.put(direction, boundingBox);
    }

    public boolean isColliding() { return colliding; }
	public float getTimeRemaining() { return timeRemaining; }
	public Rectangle getBoundingBox() { return (Rectangle) boundingBox.clone(); }
    public CollisionBehavior getBehavior() { return behavior; }
    public boolean isOneWayPlatform() { return oneWayPlatform; }
    public float getMass() { return mass; }
    public void setMass(float m) { this.mass = m; }
    
}
