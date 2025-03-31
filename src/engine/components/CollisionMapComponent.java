package engine.components;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Map;
import engine.Component;
import engine.Entity;
import world.TileMap;

public class CollisionMapComponent extends Component {
    private final boolean[][] solidTiles;
    private final int tileWidth;
    private final int tileHeight;
    private final Map<Point, Float> collidedTiles; // Mappa delle tile in collisione con il timer

    public static final float COLLISION_VISUAL_DURATION = 0.5f; // Durata in secondi

    public CollisionMapComponent(Entity entity, TileMap tileMap) {
        super(entity);
        this.tileWidth = tileMap.getTileWidth();
        this.tileHeight = tileMap.getTileHeight();

        // Genera una mappa di solidità
        this.solidTiles = tileMap.getSolidMap();
        this.collidedTiles = tileMap.getCollidedTiles();
    }

    public boolean isSolidTile(int row, int col) {
        return row >= 0 && row < solidTiles.length && col >= 0 && col < solidTiles[0].length && solidTiles[row][col];
    }

    public void update(float deltaTime) {
        // Aggiorna il timer delle tile in collisione
        Iterator<Map.Entry<Point, Float>> iterator = collidedTiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Point, Float> entry = iterator.next();
            float timeRemaining = entry.getValue() - deltaTime;
            if (timeRemaining <= 0) {
                iterator.remove(); // Rimuove la tile se il timer è scaduto
            } else {
                entry.setValue(timeRemaining);
            }
        }
    }
    
    public void setCollidedTile(int row, int col) {
        Point tilePos = new Point(col, row);
        collidedTiles.put(tilePos, COLLISION_VISUAL_DURATION); // Aggiunge la tile con il timer
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

	public boolean[][] getSolidTiles() {
		return solidTiles;
	}

	public int getMapWidth() {
		return solidTiles[0].length;
	}

	public int getMapHeight() {
		return solidTiles.length;
	}

	public Rectangle getSolidBox(int row, int col) {
		return new Rectangle(col * tileWidth, row * tileHeight, tileWidth, tileHeight);	
	}
}