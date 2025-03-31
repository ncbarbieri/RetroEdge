package engine.components;

import java.awt.Rectangle;

import engine.Component;
import engine.Entity;
import pathfinder.JumpPointSearchHV;
import pathfinder.Node;
import pathfinder.ObstacleInflationUtil;
import pathfinder.PathFinder;
import world.TileMap;

public class ChaseComponent extends Component {
    private final PathFinder pathfinder; // Algoritmo di pathfinding
    private final Entity target;        // Bersaglio da inseguire
    private final TileMap tileManager;  // Gestore della mappa di tile

    private int lastNodeRow = -1; // Riga dell'ultimo nodo calcolato
    private int lastNodeCol = -1; // Colonna dell'ultimo nodo calcolato
    private float offsetX = 0f;  // Offset orizzontale applicato
    private float offsetY = 0f;  // Offset verticale applicato
    private Node lastDeviatedTile = null; // Nodo dell'ultima deviazione calcolata

    public ChaseComponent(Entity entity, Entity target, TileMap tileManager) {
        super(entity);
		boolean[][] worldMap = tileManager.getSolidMap();
        ColliderComponent targetCollider = entity.getComponent(ColliderComponent.class);
        if(targetCollider!=null) {
        	Rectangle targetBox = targetCollider.getBoundingBox();
        	worldMap = ObstacleInflationUtil.inflateObstacles(
    				worldMap, 
    			    tileManager.getTileWidth(), tileManager.getTileHeight(), 
    			    targetBox.width, targetBox.height
    			);
        	
        }
	    // Now pass inflatedGrid to JPS
        this.pathfinder = new JumpPointSearchHV(worldMap);
        this.target = target;
        this.tileManager = tileManager;
    }

    // Getter per il pathfinder
    public PathFinder getPathfinder() {
        return pathfinder;
    }

    // Getter per il target
    public Entity getTarget() {
        return target;
    }

    // Getter per il gestore delle tile
    public TileMap getTileManager() {
        return tileManager;
    }

    // Getter e setter per l'ultimo nodo
    public int getLastNodeRow() {
        return lastNodeRow;
    }

    public int getLastNodeCol() {
        return lastNodeCol;
    }

    public void setLastNode(int row, int col) {
        this.lastNodeRow = row;
        this.lastNodeCol = col;
    }

    // Getter e setter per gli offset
    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    // Metodo per impostare offset in un'unica chiamata
    public void setOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    // Getter e setter per l'ultima tile deviata
    public Node getLastDeviatedTile() {
        return lastDeviatedTile;
    }

    public void setLastDeviatedTile(Node lastDeviatedTile) {
        this.lastDeviatedTile = lastDeviatedTile;
    }

    // Metodo per resettare i valori legati alle deviazioni
    public void resetDeviation() {
        this.lastDeviatedTile = null;
    }
}