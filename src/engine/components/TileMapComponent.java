/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package engine.components;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;
import engine.Component;
import engine.Entity;
import main.GamePanel;
import world.Tile;
import world.TileMap;

public class TileMapComponent extends Component {
    private TileMap tileMap;
    private final boolean[][] solidTiles;
    private BufferedImage[] tileMapFrames; // Lista delle immagini pre-renderizzate per ogni frame
    private final int tileWidth;
    private final int tileHeight;
    private int visibleRows;
    private int visibleCols;
    private int currentFrame = 0; // Frame corrente della tilemap
    private int totalFrames; // Numero totale di frame
    private float frameDuration; // Durata di ciascun frame in secondi
    private float frameTimer; // Timer per aggiornare il frame corrente
    protected final Map<Point, Float> collidedTiles; // Mappa delle tile in collisione con il timer

    public TileMapComponent(Entity entity, TileMap tileMap) {
        super(entity);
        this.tileMap = tileMap;
        this.solidTiles = tileMap.getSolidMap();
        this.tileWidth = tileMap.getTileWidth();
        this.tileHeight = tileMap.getTileHeight();
        this.frameDuration = tileMap.getFrameDuration();
        this.totalFrames = tileMap.getNumberOfFrames();
        this.frameTimer = 0;
        this.collidedTiles = tileMap.getCollidedTiles();
        this.visibleRows = GamePanel.GAME_HEIGHT / tileHeight;
        this.visibleCols = GamePanel.GAME_WIDTH / tileWidth;
        generateTileMapFrames();
    }

    // Genera le immagini pre-renderizzate della tilemap per ogni frame dell'animazione
    private void generateTileMapFrames() {
        totalFrames = tileMap.getNumberOfFrames();
        tileMapFrames = new BufferedImage[totalFrames];

        for (int frame = 0; frame < totalFrames; frame++) {
            BufferedImage tileMapImage = new BufferedImage(tileMap.getMapWidth() * tileMap.getTileWidth(),
                    tileMap.getMapHeight() * tileMap.getTileHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tileMapImage.createGraphics();

            for (int y = 0; y < tileMap.getMapHeight(); y++) {
                for (int x = 0; x < tileMap.getMapWidth(); x++) {
                    Tile tile = tileMap.getTile(y, x);

                    if (tile != null) {
                        if (tile.isAnimated()) {
                            // Disegna il frame corretto dell'animazione della tile
                            tile.draw(g2d, 0, 0, frame);
                        } else {
                            // Disegna la tile statica
                            tile.draw(g2d, 0, 0, 0);
                        }
                    }
                }
            }
            tileMapFrames[frame] = tileMapImage;
            g2d.dispose();
        }
    }

    public BufferedImage getTileMapImage() {
        return tileMapFrames[currentFrame];
    }

    public boolean isSolidTile(int row, int col) {
        return row >= 0 && row < solidTiles.length && col >= 0 && col < solidTiles[0].length && solidTiles[row][col];
    }

    // Metodo per aggiornare il frame corrente della tilemap in base al deltaTime
    public void update(float deltaTime) {
        frameTimer += deltaTime;
        if (frameTimer >= frameDuration) {
            frameTimer -= frameDuration;
            currentFrame = (currentFrame + 1) % totalFrames; // Aggiorna il frame
        }
    }

	public Rectangle getSolidBox(int row, int col) {
		return new Rectangle(col * tileWidth, row * tileHeight, tileWidth, tileHeight);	
	}

    public Map<Point, Float> getCollidedTiles() {
        return collidedTiles;
    }

	public TileMap getTileMap() {
        return tileMap;
    }
    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

	public int getVisibleRows() {
		return visibleRows;
	}

	public int getVisibleCols() {
		return visibleCols;
	}

	public int getMapWidth() {
		return solidTiles[0].length;
	}

	public int getMapHeight() {
		return solidTiles.length;
	}

}