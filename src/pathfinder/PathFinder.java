/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package pathfinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public abstract class PathFinder {

	protected boolean[][] grid;
    protected Node startNode;
    protected Node goalNode;
    protected Node[][] nodes;
    protected PriorityQueue<Node> openList;
    protected List<Node> path;

    public PathFinder(boolean[][] grid) {
        this.grid = grid;
        initializeNodes();
        openList = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));
    }

    public boolean setNodes(int startRow, int startCol, int goalRow, int goalCol) {
    	boolean changed = false;
    	if (startNode==null || (startNode.x!=startCol || startNode.y!=startRow)) {
            startNode = new Node(startCol, startRow);
            changed = true;
    	}
    	if (goalNode==null || (goalNode.x!=goalCol || goalNode.y!=goalRow)) {
            goalNode = new Node(goalCol, goalRow);
            changed = true;
    	}
    	return changed;
    }

    public Node getStartNode() {
		return startNode;
	}

    public Node getGoalNode() {
		return goalNode;
	}

	protected void initializeNodes() {
        int rows = grid.length;
        int cols = grid[0].length;
        nodes = new Node[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                nodes[y][x] = new Node(x, y);
                nodes[y][x].walkable = !grid[y][x]; // 0 is walkable, 1 is obstacle
            }
        }
    }

    protected void resetNodes() {
        for (Node[] row : nodes) {
            for (Node node : row) {
                node.gCost = Integer.MAX_VALUE;
                node.hCost = 0;
                node.fCost = 0;
                node.open = false;
                node.closed = false;
                node.parent = null;
            }
        }
    }

    public abstract boolean search();

    protected abstract int getHeuristic(Node a, Node b);

    protected Node getNode(int x, int y) {
		if (x >=0 && x < nodes[0].length && 
				y >= 0 && y < nodes.length)
			return nodes[y][x];
		else
			return null;
	}

	public boolean isWalkable(int x, int y) {
		return x >= 0 && x < nodes[0].length && y >= 0 && y < nodes.length && nodes[y][x].walkable;
	}

	public boolean isWalkable(Node neighbour) {
		return neighbour.x >= 0 && neighbour.x < nodes[0].length && neighbour.y >= 0 && neighbour.y < nodes.length && neighbour.walkable;
	}

    public List<Node> getPath() {
        return path;
    }
    
    protected void reconstructPath() {
        path = new ArrayList<>();
        Node current = goalNode;
        while (current != null) {
            path.add(0, current);
            current = current.parent;
        }
    }
}