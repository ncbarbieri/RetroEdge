/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */
package world;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import enums.Action;
import enums.Direction;

public class CharacterSpritesheet extends Spritesheet {
    private BufferedImage[][][] images;
    private Rectangle boundingBox;
    private Map<Direction, Rectangle> directionalBoundingBoxes; // Bounding box per direzione

    public CharacterSpritesheet(String fileName, int frameWidth, int frameHeight, int margin, int spacing) {
        super(fileName, frameWidth, frameHeight, margin, spacing);
        images = new BufferedImage[Action.values().length][Direction.values().length][];
        init();
    }

    public CharacterSpritesheet(String fileName, int frameWidth, int frameHeight) {
        super(fileName, frameWidth, frameHeight, 0, 0);
        images = new BufferedImage[Action.values().length][Direction.values().length][];
        init();
    }
    public CharacterSpritesheet(String configFile) {
    	loadSpriteData(configFile);
    }

    private void loadSpriteData(String configFile) {
        try (InputStream is = getClass().getResourceAsStream(configFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            Map<Action, Map<Direction, List<Point>>> frameData = new HashMap<>();
            Map<Action, Map<Direction, List<Boolean>>> flippedData = new HashMap<>();
            Map<Direction, Rectangle> directionalBoundingBoxes = new HashMap<>();
            String spriteFile = null;
            int frameWidth = 0, frameHeight = 0;
            Rectangle boundingBox = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("fileName:")) {
                    spriteFile = line.split(":")[1];
                } else if (line.startsWith("frameWidth:")) {
                    frameWidth = Integer.parseInt(line.split(":")[1]);
                } else if (line.startsWith("frameHeight:")) {
                    frameHeight = Integer.parseInt(line.split(":")[1]);
                } else if (line.startsWith("boundingBox:")) {
                    String[] parts = line.split(":")[1].split("-");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int width = Integer.parseInt(parts[2]);
                    int height = Integer.parseInt(parts[3]);
                    boundingBox = new Rectangle(x, y, width, height);
                } else if (line.startsWith("directionalBoundingBox:")) {
                    String[] parts = line.split(":");
                    Direction direction = Direction.valueOf(parts[1].toUpperCase());
                    String[] box = parts[2].split("-");
                    int x = Integer.parseInt(box[0]);
                    int y = Integer.parseInt(box[1]);
                    int width = Integer.parseInt(box[2]);
                    int height = Integer.parseInt(box[3]);
                    directionalBoundingBoxes.put(direction, new Rectangle(x, y, width, height));
                } else if (line.startsWith("action:")) {
                    String[] parts = line.split(":");
                    Action action = Action.valueOf(parts[1].toUpperCase());
                    Direction direction = Direction.valueOf(parts[2].toUpperCase());

                    List<Point> frames = new ArrayList<>();
                    List<Boolean> flippedFrames = new ArrayList<>();
                    String[] frameCoords = parts[3].split(",");
                    for (String coord : frameCoords) {
                        boolean isFlipped = coord.contains("(f)");
                        coord = coord.replace("(f)", ""); // Rimuovi il flip
                        String[] xy = coord.split("-");
                        frames.add(new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
                        flippedFrames.add(isFlipped);
                    }

                    frameData.computeIfAbsent(action, k -> new HashMap<>())
                             .put(direction, frames);
                    flippedData.computeIfAbsent(action, k -> new HashMap<>())
                    	.put(direction, flippedFrames);
                } 
            }

            if (spriteFile != null) {
                initialize(spriteFile, frameWidth, frameHeight, 0, 0); // Inizializza il spritesheet
//                initializeFrames(frameData); // Popola l'array images
                initializeFrames(frameData, flippedData); // Popola l'array images
                this.boundingBox = boundingBox; // Imposta la bounding box
                this.directionalBoundingBoxes = directionalBoundingBoxes;
            }

        } catch (Exception e) {
            System.err.println("Error loading sprite data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
	private void initializeFrames(Map<Action, Map<Direction, List<Point>>> frameData,
			Map<Action, Map<Direction, List<Boolean>>> flippedData) {
		this.images = new BufferedImage[Action.values().length][Direction.values().length][];

		for (Map.Entry<Action, Map<Direction, List<Point>>> actionEntry : frameData.entrySet()) {
			Action action = actionEntry.getKey();
			Map<Direction, List<Point>> directions = actionEntry.getValue();

			for (Map.Entry<Direction, List<Point>> directionEntry : directions.entrySet()) {
				Direction direction = directionEntry.getKey();
				List<Point> frames = directionEntry.getValue();
				List<Boolean> flippedFrames = flippedData.get(action).get(direction);

				images[action.getActionIndex()][direction.getDirectionIndex()] = new BufferedImage[frames.size()];
				for (int i = 0; i < frames.size(); i++) {
					Point frame = frames.get(i);
					if (flippedFrames.get(i)) {
						images[action.getActionIndex()][direction.getDirectionIndex()][i] = 
								getFlippedSprite(frame.x, frame.y);
					} else {
						images[action.getActionIndex()][direction.getDirectionIndex()][i] = 
								getSprite(frame.x, frame.y);
					}
				}
			}
		}
	}

    public Rectangle getBoundingBox() {
        if (boundingBox == null) {
            // Fallback a una bounding box predefinita se non è stata specificata
            return new Rectangle(0, 0, frameWidth, frameHeight);
        }
        return boundingBox;
    }
    
    public Map<Direction, Rectangle> getDirectionalBoundingBoxes() {
        if (directionalBoundingBoxes == null) {
            return new HashMap<>();
        }
        return directionalBoundingBoxes;
    }
    
    protected void init() {}
    
    public BufferedImage[][][] getImages() {
		return images;
	}
}