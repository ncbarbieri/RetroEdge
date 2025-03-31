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