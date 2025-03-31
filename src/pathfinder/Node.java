/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package pathfinder;

public class Node {
    public final int x, y;
    public int gCost, hCost, fCost;
    public Node parent;
    public boolean walkable = true;
    public boolean open = false;
    public boolean closed = false;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
}