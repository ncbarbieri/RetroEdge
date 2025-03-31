package pathfinder;

public class ObstacleInflationUtil {
    /**
     * Inflates obstacles so that an entity with a bounding box of size
     * (boxWidthPx x boxHeightPx) in a tile map of (tileWidthPx x tileHeightPx)
     * cannot pass near walls that are too close.
     *
     * @param originalGrid the original grid of size [mapHeight][mapWidth],
     *                     where true = solid tile, false = free tile
     * @param mapWidth the number of columns
     * @param mapHeight the number of rows
     * @param tileWidthPx the width in pixels of a tile
     * @param tileHeightPx the height in pixels of a tile
     * @param boxWidthPx the bounding box width in pixels of the entity
     * @param boxHeightPx the bounding box height in pixels of the entity
     * @return a new boolean[][] of the same size, with inflated obstacles
     */
    public static boolean[][] inflateObstacles(
            boolean[][] originalGrid,
            int tileWidthPx,
            int tileHeightPx,
            int boxWidthPx, 
            int boxHeightPx) 
    {
    	int mapHeight = originalGrid.length;
    	int mapWidth = originalGrid[0].length; 
        // Clone original grid to avoid modifying it in-place
        boolean[][] inflatedGrid = new boolean[mapHeight][mapWidth];
        for (int r = 0; r < mapHeight; r++) {
            System.arraycopy(originalGrid[r], 0, inflatedGrid[r], 0, mapWidth);
        }

        // Compute how many tiles to inflate around each solid tile
        int boxWidthTiles  = (int) Math.ceil((float) boxWidthPx  / tileWidthPx);
        int boxHeightTiles = (int) Math.ceil((float) boxHeightPx / tileHeightPx);

        // Take the "radius" (half bounding box) in tile units
        // For a bounding box 3 tiles wide, we want to inflate 1 tile around it, etc.
        int inflateX = (int) Math.floor((boxWidthTiles  - 1) / 2.0);
        int inflateY = (int) Math.floor((boxHeightTiles - 1) / 2.0);

        // For a simpler approach, we can unify them:
        int inflate = Math.max(inflateX, inflateY);

        // Mark around each solid tile
        for (int row = 0; row < mapHeight; row++) {
            for (int col = 0; col < mapWidth; col++) {
                if (originalGrid[row][col]) {
                    // This tile is solid => inflate
                    for (int dr = -inflate; dr <= inflate; dr++) {
                        for (int dc = -inflate; dc <= inflate; dc++) {
                            int nr = row + dr;
                            int nc = col + dc;
                            if (nr >= 0 && nr < mapHeight && nc >= 0 && nc < mapWidth) {
                                inflatedGrid[nr][nc] = true;
                            }
                        }
                    }
                }
            }
        }

        return inflatedGrid;
    }
}
