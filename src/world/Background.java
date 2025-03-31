package world;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class Background {
    private BufferedImage image;
    private float speed;
    private int yPosition;
    
    public Background(String fileName, float speed, int yPosition) {
		super();
		this.image = loadImage(fileName);
		this.speed = speed;
		this.yPosition = yPosition;
	}

	public BufferedImage getImage() {
		return image;
	}

	public float getSpeed() {
		return speed;
	}

	public int getYPosition() {
		return yPosition;
	}

	private BufferedImage loadImage(String fileName) {
		InputStream is = getClass().getResourceAsStream(fileName);
		BufferedImage image = null;
		try {
			image = ImageIO.read(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// to free resources
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return image;
	}
}