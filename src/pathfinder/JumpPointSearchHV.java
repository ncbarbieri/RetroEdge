/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package pathfinder;

import java.util.*;

public class JumpPointSearchHV extends PathFinder {

	public JumpPointSearchHV(boolean[][] grid) {
		super(grid);
	}

	@Override
	public boolean search() {
		if (startNode.y>=0 && startNode.y<nodes.length &&
				startNode.x>=0 && startNode.x<nodes[0].length) {
			startNode = nodes[startNode.y][startNode.x];
		}
		if (goalNode.y>=0 && goalNode.y<nodes.length &&
				goalNode.x>=0 && goalNode.x<nodes[0].length) {
			goalNode = nodes[goalNode.y][goalNode.x];
		}
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

			identifySuccessors(current);
		}

		return false; // Path not found
	}

	private void identifySuccessors(Node current) {
		List<Node> neighbours = findNeighbours(current);
		for (Node neighbour : neighbours) {
			Node jumpNode = jump(current, neighbour);

			if (jumpNode != null && !jumpNode.closed) {
				int newGCost = current.gCost + getHeuristic(current, jumpNode);
				if (!jumpNode.open || newGCost < jumpNode.gCost) {
					jumpNode.gCost = newGCost;
					jumpNode.hCost = getHeuristic(jumpNode, goalNode);
					jumpNode.fCost = jumpNode.gCost + jumpNode.hCost;
					jumpNode.parent = current;

					if (!jumpNode.open) {
						openList.add(jumpNode);
						jumpNode.open = true;
					}
				}
			}
		}
	}

	private Node jump(Node current, Node neighbour) {
        if (neighbour == null || !isWalkable(neighbour))
        	return null;
        if (neighbour.equals(goalNode)) 
        	return neighbour;

        int dx = neighbour.x - current.x;
        int dy = neighbour.y - current.y;

        // check for forced neighbors
        // check horizontally/vertically
        if (dx != 0) {
            if ((isWalkable(neighbour.x, neighbour.y + 1) && !isWalkable(neighbour.x - dx, neighbour.y + 1)) ||
                    (isWalkable(neighbour.x, neighbour.y - 1) && !isWalkable(neighbour.x - dx, neighbour.y - 1))) {
                return neighbour;
            }
        } else if (dy != 0) {
            if ((isWalkable(neighbour.x + 1, neighbour.y) && !isWalkable(neighbour.x + 1, neighbour.y - dy)) ||
                    (isWalkable(neighbour.x - 1, neighbour.y) && !isWalkable(neighbour.x - 1, neighbour.y - dy))) {
                return neighbour;
            }
            // when moving vertically check for horizontal jump points
            if (jump(neighbour, getNode(neighbour.x + 1, neighbour.y)) != null  ||
                    jump(neighbour, getNode(neighbour.x - 1, neighbour.y)) != null) {
                return neighbour;
            }
        } else {
            return null;
        }
        
    	return jump(neighbour, getNode(neighbour.x + dx, neighbour.y + dy));
	}

	private List<Node> findNeighbours(Node node) {
        List<Node> neighbours = new ArrayList<>();
        if (node.parent != null) {
            int dx = (node.x - node.parent.x) / Math.max(Math.abs(node.x - node.parent.x), 1);
            int dy = (node.y - node.parent.y) / Math.max(Math.abs(node.y - node.parent.y), 1);

            if (dx != 0) { // Horizontal movement
                if (isWalkable(node.x + dx, node.y))
                    neighbours.add(nodes[node.y][node.x + dx]);
                if (isWalkable(node.x, node.y + 1))
                    neighbours.add(nodes[node.y + 1][node.x]);
                if (isWalkable(node.x, node.y - 1))
                    neighbours.add(nodes[node.y - 1][node.x]);
            } else if (dy != 0) { // Vertical movement
                if (isWalkable(node.x, node.y + dy))
                    neighbours.add(nodes[node.y + dy][node.x]);
                if (isWalkable(node.x + 1, node.y))
                    neighbours.add(nodes[node.y][node.x + 1]);
                if (isWalkable(node.x - 1, node.y))
                    neighbours.add(nodes[node.y][node.x - 1]);
            }
        } else {
            // No parent, return all adjacent nodes (no diagonals)
            int x = node.x;
            int y = node.y;

            if (isWalkable(x + 1, y))
                neighbours.add(nodes[y][x + 1]);
            if (isWalkable(x - 1, y))
                neighbours.add(nodes[y][x - 1]);
            if (isWalkable(x, y + 1))
                neighbours.add(nodes[y + 1][x]);
            if (isWalkable(x, y - 1))
                neighbours.add(nodes[y - 1][x]);
        }
        return neighbours;
    }

	protected int getHeuristic(Node a, Node b) {
		// Manhattan distance for grids without diagonal movement
		int dx = Math.abs(a.x - b.x);
		int dy = Math.abs(a.y - b.y);
		int D = 10; // Cost for horizontal or vertical movement
		return D * (dx + dy);
	}
}