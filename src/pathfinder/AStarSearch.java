package pathfinder;

import java.util.ArrayList;
import java.util.List;

public class AStarSearch extends PathFinder {

    public AStarSearch(boolean[][] grid) {
        super(grid);
    }

	@Override
    public boolean search() {
        startNode = nodes[startNode.y][startNode.x];
        goalNode = nodes[goalNode.y][goalNode.x];
        openList.clear();
        resetNodes();

        startNode.gCost = 0;
        startNode.hCost = getHeuristic(startNode, goalNode);
        startNode.fCost = startNode.gCost + startNode.hCost;
        openList.add(startNode);
        startNode.open = true;

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            current.closed = true;

            if (current == goalNode) {
                reconstructPath();
                return true;
            }

            List<Node> neighbours = getNeighbours(current);
            for (Node neighbour : neighbours) {
                if (neighbour.closed || !neighbour.walkable) {
                    continue;
                }

                int tentativeGCost = current.gCost + getMovementCost(current, neighbour);
                if (!neighbour.open || tentativeGCost < neighbour.gCost) {
                    neighbour.gCost = tentativeGCost;
                    neighbour.hCost = getHeuristic(neighbour, goalNode);
                    neighbour.fCost = neighbour.gCost + neighbour.hCost;
                    neighbour.parent = current;

                    if (!neighbour.open) {
                        openList.add(neighbour);
                        neighbour.open = true;
                    }
                }
            }
        }

        return false; // Path not found
    }

    private List<Node> getNeighbours(Node node) {
        List<Node> neighbours = new ArrayList<>();
        int x = node.x;
        int y = node.y;

        // Define movements (up, down, left, right)
        int[][] movements = {
            { 0, -1 }, // Up
            { 0, 1 },  // Down
            { -1, 0 }, // Left
            { 1, 0 }   // Right
        };

        for (int[] move : movements) {
            int nx = x + move[0];
            int ny = y + move[1];
            if (isWalkable(nx, ny)) {
                neighbours.add(nodes[ny][nx]);
            }
        }

        return neighbours;
    }

    protected int getHeuristic(Node a, Node b) {
        // Manhattan distance for grids without diagonal movement
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        return dx + dy;
    }

    private int getMovementCost(Node a, Node b) {
        // Cost for moving from one node to another (assume cost of 1 for each move)
        return 1;
    }

}