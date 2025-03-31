package world;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import helpers.Logger;
import main.GamePanel;

public class TileMap {
    protected Tile[][] tileMap; 
    protected int visibleRows;
    protected int visibleCols;
    protected int tileWidth;
    protected int tileHeight;
    protected int numberOfFrames;  // Numero totale di frame per l'animazione
    protected float frameDuration;  // Durata di ciascun frame in secondi
    protected final Map<Point, Float> collidedTiles; // Mappa delle tile in collisione con il timer

    public TileMap(String tilesetFile, String mapFile, float frameDuration) {
		try {
			Tileset tileset = new Tileset(tilesetFile);
			tileWidth = tileset.getTileWidth();
			tileHeight = tileset.getTileHeight();
			numberOfFrames = tileset.getNumberOfFrames();
			this.frameDuration = frameDuration;
			int[][] map = getMap(mapFile);
			tileMap = new Tile[map.length][map[0].length];
			for (int i = 0; i < map.length; i++) { // -> row - y
				for (int j = 0; j < map[0].length; j++) { // -> col - x
					int tileNumber = map[i][j] - 1;
					tileMap[i][j] = tileset.createTile(tileNumber, i, j);
				}
			}
		} catch (Exception e) {
			tileMap = null;
			Logger.log("Error loading TileMap.", e);
		}
        this.collidedTiles = new HashMap<>();
		visibleRows = GamePanel.GAME_HEIGHT / tileHeight;
		visibleCols = GamePanel.GAME_WIDTH / tileWidth;
    }

    // Metodo per caricare la mappa da un file di testo
    protected int[][] loadMap(String filePath, int rows, int cols) {
        int[][] map = new int[rows][cols];
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            for (int row = 0; row < rows; row++) {
                line = br.readLine();
                String[] values = line.split(",");
                for (int col = 0; col < cols; col++) {
                    map[row][col] = Integer.parseInt(values[col].trim());
                }
            }
            br.close();
        } catch (Exception e) {
        	map = null;
			Logger.log("Error loading map.", e);
        }
        return map;
    }

    protected Dimension getMapDimensionsFromFile(String fileName) {
        Dimension mapDimensions = null;
        try {
	        InputStream is = getClass().getResourceAsStream(fileName);
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            if (line != null) {
                String[] values = line.split(",");
                int mapWidth = values.length; // Numero di colonne (larghezza)
                int mapHeight = 1; // Numero di righe almeno una

                // Continua a contare le righe per determinare l'altezza
                while ((line = br.readLine()) != null) {
                	mapHeight++;
                }

                mapDimensions = new Dimension(mapWidth, mapHeight);
            }
            br.close();
        } catch (IOException e) {
			Logger.log("Error loading map dimensions.", e);
        }
        return mapDimensions;
    }    

    protected int[][] getMap(String filePath) {
    	int[][] result = null;
    	Dimension mapDimension = getMapDimensionsFromFile(filePath);
    	if(mapDimension!=null) {
        	result = loadMap(filePath, mapDimension.height, mapDimension.width);
    	}
    	return result;
    }

    public boolean[][] getSolidMap() {
        boolean[][] solidTiles = new boolean[tileMap.length][tileMap[0].length];

        for (int row = 0; row < tileMap.length; row++) {
            for (int col = 0; col < tileMap[0].length; col++) {
                solidTiles[row][col] = tileMap[row][col] != null && tileMap[row][col].isSolid();
            }
        }
        
    	return solidTiles;
    }

    public Map<Point, Float> getCollidedTiles() {
        return collidedTiles;
    }

    // Restituisce il numero di colonne visibili
    public int getVisibleCols() {
        return visibleCols;
    }

    // Restituisce il numero di righe visibili
    public int getVisibleRows() {
        return visibleRows;
    }

    // Restituisce la larghezza totale del mondo in pixel
    public int getWorldWidth() {
        return this.tileMap[0].length * tileWidth;
    }

    // Restituisce l'altezza totale del mondo in pixel
    public int getWorldHeight() {
        return this.tileMap.length * tileHeight;
    }

    public Tile getTile(int row, int col) {
        Tile tile = null;
        if (row >= 0 && row < tileMap.length && col >= 0 && col < tileMap[0].length) {
            tile = tileMap[row][col];
        }
        return tile;
    }

    public int getMapHeight() {
        return this.tileMap.length;
    }

    public int getMapWidth() {
        return this.tileMap[0].length;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }
    
    public int getNumberOfFrames() {
        return this.numberOfFrames;  // Restituisce il numero totale di frame
    }

    public float getFrameDuration() {
        return this.frameDuration;  // Restituisce la durata del frame in secondi
    }
}