package world;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
    private BufferedImage[] images;
    private boolean animated;
    private boolean solid;
    private Rectangle solidBox;
    private int row;
    private int column;
    private int tileWidth;
    private int tileHeight;

    // Costruttore per tile statiche
    public Tile(BufferedImage image, boolean solid, int row, int column) {
        this(new BufferedImage[] { image }, solid, row, column, false, 0);
    }

    // Costruttore per tile animate
    public Tile(BufferedImage[] animatedTileImages, boolean solid, int row, int column, float timePerFrame) {
        this(animatedTileImages, solid, row, column, true, timePerFrame);
    }

    // Costruttore interno comune
    private Tile(BufferedImage[] images, boolean solid, int row, int column, boolean animated, float timePerFrame) {
        this.row = row;
        this.column = column;
        this.tileWidth = images[0].getWidth();
        this.tileHeight = images[0].getHeight();
        this.images = images;
        this.animated = animated;
        this.solid = solid;
        this.solidBox = new Rectangle(column * tileWidth, row * tileHeight, tileWidth, tileHeight);
    }

    public boolean isSolid() {
        return this.solid;
    }

    public Rectangle getSolidBox() {
        return (Rectangle) this.solidBox.clone();
    }

    // Metodo per disegnare la tile con il frame specificato
    public void draw(Graphics2D g, int xOffset, int yOffset, int frameNumber) {
        g.drawImage(images[animated ? frameNumber : 0], tileWidth * this.column - xOffset, tileHeight * this.row - yOffset, null);
    }

    public boolean isAnimated() {
        return animated;
    }
}