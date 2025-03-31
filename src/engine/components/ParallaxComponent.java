package engine.components;

import java.awt.image.BufferedImage;
import engine.Component;
import engine.Entity;
import world.Background;
import world.TileMap;

public class ParallaxComponent extends Component {
    private BufferedImage image;
    private float speed;
    private int yPosition;
    private int worldWidth, worldHeight;

	public ParallaxComponent(Entity entity, TileMap world, Background background) {
		super(entity);
		this.image = background.getImage();
		this.speed = background.getSpeed();
		this.yPosition = background.getYPosition();
		this.worldWidth = world.getWorldWidth();
		this.worldHeight = world.getWorldHeight();
	}

	public BufferedImage getImage() { return image; }
	public int getYPosition() { return yPosition; }
	public float getSpeed() { return speed; }
	public int getWorldWidth() { return worldWidth; }
	public int getWorldHeight() { return worldHeight; }
}
