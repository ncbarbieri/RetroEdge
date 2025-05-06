/**
 * RetroEdge Educational Game Engine
 * 
 * Copyright (c) 2025 Nicola Christian Barbieri
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en
 */

package engine.systems;

import engine.BaseSystem;
import engine.Engine;
import engine.components.ColliderComponent;
import engine.components.CollisionMapComponent;
import engine.components.GravityComponent;
import engine.components.InteractionComponent;
import engine.components.MotionComponent;
import engine.Entity;
import enums.CollisionBehavior;
import enums.EngineState;
import pathfinder.Node;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollisionSystem extends BaseSystem {

	private float restitution = 0.2f;
    static final int tolerance = 0; // Tolleranza in pixel
    private final Set<Node> collisions = new HashSet<>(); // Set per le collisioni

	public CollisionSystem(Engine engine) {
        super(engine, 4); // Priorità del sistema
    }

    @Override
    protected void initStateUpdateMap() {
        // Customize states where this system updates
    	setUpdateInState(EngineState.STARTING, true);
    	setUpdateInState(EngineState.RUNNING, true);
    	setUpdateInState(EngineState.CUTSCENE, false);
    	setUpdateInState(EngineState.PAUSED, false);
    	setUpdateInState(EngineState.SHOWING_DIALOG, false);
    	setUpdateInState(EngineState.EXITING, false);
    	setUpdateInState(EngineState.ENTERING, true);
    }

	public float getRestitution() { return restitution; }
	public void setRestitution(float restitution) { this.restitution = restitution; }

	@Override
    public void update(float deltaTime) {
        // Aggiorna le collisioni tra entità e tile
        engine.accessEntities(entities -> {
        	// 1. Tilemap Collision detection & resolution
            for (Entity entity : entities) {
    			resolveTileCollisions(entity, deltaTime);
            }
            
            // 2. Collision between entities detection & resolution
            int size = entities.size();
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    Entity e1 = entities.get(i);
                    Entity e2 = entities.get(j);
                    handleCollision(e1, e2);
                }
            }
            
            // 3) Tile collisions second pass
            for (Entity e : entities) {
                resolveTileCollisions(e, deltaTime);
            }
            
			for (Entity e : entities) {
				checkAndSetOnGround(e, entities);
			}
        });
    }
	
	/*
	 * Tilemap collision
	 */

	private void resolveTileCollisions(Entity entity, float deltaTime) {
		MotionComponent motion = entity.getComponent(MotionComponent.class);
	    ColliderComponent col = entity.getComponent(ColliderComponent.class);
	    CollisionMapComponent map = entity.getComponent(CollisionMapComponent.class);
	    
	    if (motion == null || col == null || map == null) {
	        return;
	    }

	    // Bounding box dell'entità per il controllo iniziale
	    Rectangle box = col.getBoundingBox();
	    int boxX = (int) (motion.getX() + box.x);
	    int boxY = (int) (motion.getY() + box.y);

	    // Controllo preliminare: se l'entità non è in collisione, esci
	    if (!findCollisionsInRect(boxX, boxY, box.width, box.height, map)) {
	        return;
	    }
	    
        InteractionComponent interaction = entity.getComponent(InteractionComponent.class);

        // Aggiunge le tile coinvolte al rispettivo Set
        if (interaction != null) {
        	interaction.clearCollisionTiles();
        	interaction.addCollisionTiles(collisions);
        }
	    
	    // Current attempted final position
	    float currentX = motion.getX();
	    float currentY = motion.getY();

	    // Previous position
	    float previousX = motion.getOldX();
	    float previousY = motion.getOldY();

	    // Resolve horizontal collision
	    float correctedX = resolveHorizontalCollision(motion, col, map, previousX, currentX, previousY);
	    if (correctedX != currentX) {
	    	if(engine.isDebug())
	    		col.collides();
		    motion.setX(correctedX);
//		    motion.setDeltaX(correctedX - previousX);
	        motion.setVx((correctedX - previousX) / deltaTime); // Blocca la velocità orizzontale
	    }

	    // After horizontal correction, resolve vertical collision
	    float correctedY = resolveVerticalCollision(motion, col, map, correctedX, previousY, currentY);
	    if (correctedY != currentY) {
	    	if (engine.isDebug())
	    		col.collides();
		    motion.setY(correctedY);
//		    motion.setDeltaY(correctedY - previousY);
	        motion.setVy((correctedY - previousY) / deltaTime); // Blocca la velocità verticale
	        // Here is the gravity logic if we want to check if we are moving up or down
	        GravityComponent grav = entity.getComponent(GravityComponent.class);
	        if (grav != null) {
	            // if (curY > oldY) => moving down
	            // else => moving up
	            if (currentY > previousY) {
	                grav.setOnGround(true);
	            } else {
	                grav.setFallSpeedAfterCollision();
	            }
	        }
	    }
 	}

	private float resolveHorizontalCollision(MotionComponent pos, ColliderComponent col, CollisionMapComponent map,
	                                         float prevX, float curX, float baseY) {
	    // If no horizontal movement
	    if (curX == prevX) {
	        return curX;
	    }

	    Rectangle box = col.getBoundingBox();
	    // Check if the attempted position is free
	    if (canMoveInRect((int)(curX + box.x), (int)(baseY + box.y), box.width, box.height, map)) {
	        return curX; // No collision
	    }

	    // Collision detected: binary search for the closest free position
	    float correctedX = binarySearchFreePositionX(prevX, curX, baseY, box, map);
	    return correctedX;
	    
	}

	private float resolveVerticalCollision(MotionComponent pos, ColliderComponent col, CollisionMapComponent map,
	                                       float baseX, float prevY, float curY) {
	    // If no vertical movement
	    if (curY == prevY) {
	        return curY;
	    }

	    Rectangle box = col.getBoundingBox();
	    // Check if the attempted position is free
	    if (canMoveInRect((int)(baseX + box.x), (int)(curY + box.y), box.width, box.height, map)) {
	        return curY; // No collision
	    }

	    // Collision detected: binary search for the closest free position
	    float correctedY = binarySearchFreePositionY(baseX, prevY, curY, box, map);
	    return correctedY;
	}

    public Set<Node> canMove(int boxX, int boxY, int width, int height, CollisionMapComponent map) {
        // Pulisci il set di collisioni prima di ogni utilizzo
        collisions.clear();

        if (boxX < -tolerance || boxY < -tolerance) return collisions; // Nessuna collisione
        int mapWidthPx = map.getMapWidth() * map.getTileWidth();
        int mapHeightPx = map.getMapHeight() * map.getTileHeight();
        if (boxX + width > mapWidthPx + tolerance || boxY + height > mapHeightPx + tolerance) return collisions;

        int tileW = map.getTileWidth();
        int tileH = map.getTileHeight();

        int leftCol = (boxX + tolerance) / tileW;
        int rightCol = (boxX + width - tolerance) / tileW;
        int topRow = (boxY + tolerance) / tileH;
        int bottomRow = (boxY + height - tolerance) / tileH;

        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (map.isSolidTile(row, col)) {
                    Rectangle tileRect = map.getSolidBox(row, col);
                    Rectangle boxRect = new Rectangle(boxX, boxY, width, height);
                    if (boxRect.intersects(tileRect)) {
                        collisions.add(new Node(col, row)); // Aggiungi nodo in collisione
                        if (engine.isDebug()) {
                            map.setCollidedTile(row, col);
                        }
                    }
                }
            }
        }

        return collisions;
    }

    /**
     * Popola il set di tile con cui si verifica una collisione.
     */
    public boolean findCollisionsInRect(int boxX, int boxY, int width, int height, CollisionMapComponent map) {
		boolean found = false;
        collisions.clear(); // Svuota il set prima di iniziare

        if (boxX < -tolerance || boxY < -tolerance) return true;
        int mapWidthPx = map.getMapWidth() * map.getTileWidth();
        int mapHeightPx = map.getMapHeight() * map.getTileHeight();
        if (boxX + width > mapWidthPx + tolerance || boxY + height > mapHeightPx + tolerance) return true;

        int tileW = map.getTileWidth();
        int tileH = map.getTileHeight();

        int leftCol = (boxX + tolerance) / tileW;
        int rightCol = (boxX + width - tolerance) / tileW;
        int topRow = (boxY + tolerance) / tileH;
        int bottomRow = (boxY + height - tolerance) / tileH;

        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (map.isSolidTile(row, col)) {
                    Rectangle tileRect = map.getSolidBox(row, col);
                    Rectangle boxRect = new Rectangle(boxX, boxY, width, height);
                    if (boxRect.intersects(tileRect)) {
                    	found = true;
                        collisions.add(new Node(col, row));
                        if (engine.isDebug()) {
                            map.setCollidedTile(row, col);
                        }
                    }
                }
            }
        }
        return found;
    }
    
    private boolean canMoveInRect(int boxX, int boxY, int width, int height, CollisionMapComponent map) {
		boolean canMove = true;

	    if (boxX < -tolerance || boxY < -tolerance) return false;
	    int mapWidthPx = map.getMapWidth() * map.getTileWidth();
	    int mapHeightPx = map.getMapHeight() * map.getTileHeight();
	    if (boxX + width > mapWidthPx + tolerance || boxY + height > mapHeightPx + tolerance) return false;

	    int tileW = map.getTileWidth();
	    int tileH = map.getTileHeight();

	    int leftCol = (boxX + tolerance) / tileW;
	    int rightCol = (boxX + width - tolerance) / tileW;
	    int topRow = (boxY + tolerance) / tileH;
	    int bottomRow = (boxY + height - tolerance) / tileH;
        Rectangle boxRect = new Rectangle(boxX, boxY, width, height);

	    for (int row = topRow; row <= bottomRow; row++) {
	        for (int col = leftCol; col <= rightCol; col++) {
	            if (map.isSolidTile(row, col)) {
	                Rectangle tileRect = map.getSolidBox(row, col);
	                if (boxRect.intersects(tileRect)) {
	                    canMove = false;
	                    if (engine.isDebug()) 
		                    map.setCollidedTile(row, col);
	                    else
	                    	return false;
	                }
	            }
	        }
	    }

	    return canMove;
	}
	
	private float binarySearchFreePositionX(float startX, float endX, float fixedY, Rectangle box, CollisionMapComponent map) {
	    float low = startX;
	    float high = endX;
	    float lastFreePosition = startX;

	    for (int i = 0; i < 20; i++) {
	        float mid = (low + high) / 2f;

	        int boxX = (int) (mid + box.x);
	        int boxY = (int) (fixedY + box.y);

	        if (canMoveInRect(boxX, boxY, box.width, box.height, map)) {
	            lastFreePosition = mid;
	            if (endX > startX) low = mid;
	            else high = mid;
	        } else {
	            if (endX > startX) high = mid;
	            else low = mid;
	        }

	        // Utilizza il delta più grande per la convergenza
	        if (Math.abs(high - low) < 1.0f) {
	            break;
	        }
	    }

	    return canMoveInRect((int)(lastFreePosition + box.x), (int)(fixedY + box.y), box.width, box.height, map) 
	        ? lastFreePosition 
	        : startX;
	}
	
	private float binarySearchFreePositionY(float fixedX, float startY, float endY, Rectangle box, CollisionMapComponent map) {
	    float low = startY;
	    float high = endY;
	    float lastFreePosition = startY;

	    for (int i = 0; i < 20; i++) {
	        float mid = (low + high) / 2f;

	        int boxX = (int) (fixedX + box.x);
	        int boxY = (int) (mid + box.y);

	        if (canMoveInRect(boxX, boxY, box.width, box.height, map)) {
	            lastFreePosition = mid;
	            if (endY > startY) low = mid;
	            else high = mid;
	        } else {
	            if (endY > startY) high = mid;
	            else low = mid;
	        }

	        // Utilizza il delta più grande per la convergenza
	        if (Math.abs(high - low) < 1.0f) {
	            break;
	        }
	    }

	    return canMoveInRect((int)(fixedX + box.x), (int)(lastFreePosition + box.y), box.width, box.height, map) 
	        ? lastFreePosition 
	        : startY;
	}
	
	/*
	 * Collision between entities
	 */

    private void handleCollision(Entity a, Entity b) {
    	MotionComponent posA = a.getComponent(MotionComponent.class);
        ColliderComponent colA = a.getComponent(ColliderComponent.class);
        MotionComponent posB = b.getComponent(MotionComponent.class);
        ColliderComponent colB = b.getComponent(ColliderComponent.class);

        if (posA == null || colA == null || posB == null || colB == null) return;

        float ax = posA.getX() + colA.getBoundingBox().x;
        float ay = posA.getY() + colA.getBoundingBox().y;
        float aw = colA.getBoundingBox().width;
        float ah = colA.getBoundingBox().height;

        float bx = posB.getX() + colB.getBoundingBox().x;
        float by = posB.getY() + colB.getBoundingBox().y;
        float bw = colB.getBoundingBox().width;
        float bh = colB.getBoundingBox().height;

        // Check overlap
        if (ax >= bx + bw || ax + aw <= bx || ay >= by + bh || ay + ah <= by) {
            return; // no collision
        }
        
    	if (engine.isDebug()) {
            colA.collides();
            colB.collides();
    	}

        // Controlla se l'entità ha l'InteractableComponent
        InteractionComponent interactableA = a.getComponent(InteractionComponent.class);
        InteractionComponent interactableB = b.getComponent(InteractionComponent.class);

        // Aggiunge le entità coinvolte al rispettivo Set
        if (interactableA != null) {
            interactableA.getInteractionSet().add(b);
        }

        if (interactableB != null) {
            interactableB.getInteractionSet().add(a);
        }

        // Compute overlap
        float overlapX = Math.min(ax + aw, bx + bw) - Math.max(ax, bx);
        float overlapY = Math.min(ay + ah, by + bh) - Math.max(ay, by);
        boolean resolveOnX = Math.abs(overlapX) < Math.abs(overlapY);

        // One-way platform check
        if (colA.isOneWayPlatform() && shouldPassThrough(a, b, ax, ay, aw, ah, bx, by, bw, bh, overlapX, overlapY, resolveOnX))
            return;
        if (colB.isOneWayPlatform() && shouldPassThrough(b, a, bx, by, bw, bh, ax, ay, aw, ah, overlapX, overlapY, resolveOnX))
            return;

        CollisionBehavior behaviorA = colA.getBehavior();
        CollisionBehavior behaviorB = colB.getBehavior();

        float massA = colA.getMass();
        float massB = colB.getMass();

        if (behaviorA == CollisionBehavior.STATIC && behaviorB == CollisionBehavior.STATIC) {
            // Both static - no move
            return;
        } else if (behaviorA == CollisionBehavior.STATIC && behaviorB == CollisionBehavior.DYNAMIC) {
            resolveCollisionSingle(posA, posB, overlapX, overlapY, resolveOnX, behaviorA, behaviorB, ax, ay, aw, ah, bx, by, bw, bh);
            applyRestitution(a, b, resolveOnX, ax, ay, aw, ah, bx, by, bw, bh);
        } else if (behaviorB == CollisionBehavior.STATIC && behaviorA == CollisionBehavior.DYNAMIC) {
            resolveCollisionSingle(posB, posA, overlapX, overlapY, resolveOnX, behaviorB, behaviorA, bx, by, bw, bh, ax, ay, aw, ah);
            applyRestitution(a, b, resolveOnX, ax, ay, aw, ah, bx, by, bw, bh);
        } else {
            // Both dynamic
            float totalMass = massA + massB;
            float ratioA = (massB / totalMass);
            float ratioB = (massA / totalMass);

            resolveCollisionDouble(posA, posB, overlapX, overlapY, resolveOnX, ratioA, ratioB, ax, ay, aw, ah, bx, by, bw, bh);
            applyRestitution(a, b, resolveOnX, ax, ay, aw, ah, bx, by, bw, bh);
        }
    }

    private void resolveCollisionSingle(MotionComponent posStaticSide, MotionComponent posDynamicSide,
                                        float overlapX, float overlapY, boolean resolveOnX,
                                        CollisionBehavior behaviorStatic, CollisionBehavior behaviorDynamic,
                                        float ax, float ay, float aw, float ah,
                                        float bx, float by, float bw, float bh) {
        // We know one is static (posStaticSide) and the other dynamic (posDynamicSide),
        // but actually we only need to move the dynamic one.
        // The parameters are a bit rearranged because we reuse the same method for both cases.

        // Decide normal direction by comparing centers
        float aCenterX = ax + aw * 0.5f;
        float aCenterY = ay + ah * 0.5f;
        float bCenterX = bx + bw * 0.5f;
        float bCenterY = by + bh * 0.5f;

        // The dynamic entity is posDynamicSide. We push it away from the static.
        if (resolveOnX) {
            // Push dynamic entity along X
            if (aCenterX < bCenterX) {
                // dynamic is on right side -> dynamic goes right
                posDynamicSide.setX(posDynamicSide.getX() + overlapX);
            } else {
                // dynamic is on left side -> dynamic goes left
                posDynamicSide.setX(posDynamicSide.getX() - overlapX);
            }
        } else {
            // Push dynamic entity along Y
            if (aCenterY < bCenterY) {
                // dynamic is below -> dynamic goes down
                posDynamicSide.setY(posDynamicSide.getY() + overlapY);
            } else {
                // dynamic is above -> dynamic goes up
                posDynamicSide.setY(posDynamicSide.getY() - overlapY);
            }
        }
    }

    private void resolveCollisionDouble(MotionComponent posA, MotionComponent posB,
                                        float overlapX, float overlapY, boolean resolveOnX,
                                        float ratioA, float ratioB,
                                        float ax, float ay, float aw, float ah,
                                        float bx, float by, float bw, float bh) {
        float aCenterX = ax + aw * 0.5f;
        float aCenterY = ay + ah * 0.5f;
        float bCenterX = bx + bw * 0.5f;
        float bCenterY = by + bh * 0.5f;

        // Both dynamic: move both proportionally.
        if (resolveOnX) {
            if (aCenterX < bCenterX) {
                // A is on left, A moves left, B moves right
                posA.setX(posA.getX() - overlapX * ratioA);
                posB.setX(posB.getX() + overlapX * ratioB);
            } else {
                // A is on right, A moves right, B moves left
                posA.setX(posA.getX() + overlapX * ratioA);
                posB.setX(posB.getX() - overlapX * ratioB);
            }
        } else {
            if (aCenterY < bCenterY) {
                // A is above, A moves up, B moves down
                posA.setY(posA.getY() - overlapY * ratioA);
                posB.setY(posB.getY() + overlapY * ratioB);
            } else {
                // A is below, A moves down, B moves up
                posA.setY(posA.getY() + overlapY * ratioA);
                posB.setY(posB.getY() - overlapY * ratioB);
            }
        }
    }

    private void applyRestitution(Entity a, Entity b, boolean resolveOnX,
                                  float ax, float ay, float aw, float ah,
                                  float bx, float by, float bw, float bh) {
    	MotionComponent velA = a.getComponent(MotionComponent.class);
    	MotionComponent velB = b.getComponent(MotionComponent.class);
        ColliderComponent colA = a.getComponent(ColliderComponent.class);
        ColliderComponent colB = b.getComponent(ColliderComponent.class);

        if (velA == null && velB == null) return;

        boolean aStatic = (colA.getBehavior() == CollisionBehavior.STATIC);
        boolean bStatic = (colB.getBehavior() == CollisionBehavior.STATIC);

        float aCenterX = ax + aw * 0.5f;
        float aCenterY = ay + ah * 0.5f;
        float bCenterX = bx + bw * 0.5f;
        float bCenterY = by + bh * 0.5f;

        float nx, ny;
        if (resolveOnX) {
            // Determine normal based on who is on the left/right
            if (aCenterX < bCenterX) {
                nx = -1; ny = 0;
            } else {
                nx = 1; ny = 0;
            }
        } else {
            // Determine normal based on who is above/below
            if (aCenterY < bCenterY) {
                nx = 0; ny = -1;
            } else {
                nx = 0; ny = 1;
            }
        }

        float massA = colA.getMass();
        float massB = colB.getMass();

        // Assume a small relative velocity for minimal bounce
        // You may want to compute actual relative velocity if desired.
        float vRelNormal = 1f;
        float j = -(1 + restitution) * vRelNormal / ((1/massA) + (1/massB));

        if (!aStatic && velA != null) {
            velA.setVx(velA.getVx() - (j * nx) / massA);
            velA.setVy(velA.getVy() - (j * ny) / massA);
        }

        if (!bStatic && velB != null) {
            velB.setVx(velB.getVx() + (j * nx) / massB);
            velB.setVy(velB.getVy() + (j * ny) / massB);
        }
    }

    private boolean shouldPassThrough(Entity platform, Entity other,
                                      float px, float py, float pw, float ph,
                                      float ox, float oy, float ow, float oh,
                                      float overlapX, float overlapY, boolean resolveOnX) {
		// If it's a one-way platform, allow passing if:
		// - The other entity is moving upwards
		// - The other entity's feet are above the platform top
		// For simplicity, assume platform top = platformPosY, other feet = posY +
		// boundingBox.height
    	MotionComponent posPlat = platform.getComponent(MotionComponent.class);
        ColliderComponent colPlat = platform.getComponent(ColliderComponent.class);
        MotionComponent posOth = other.getComponent(MotionComponent.class);

        if (posPlat == null || colPlat == null || posOth == null) return false;

        float platformTopY = posPlat.getY() + colPlat.getBoundingBox().y;
        float otherFeet = posOth.getY() + other.getComponent(ColliderComponent.class).getBoundingBox().height;
        float vy = posOth.getVy();

		// Pass through if going up or if other is below platform top?
		// Typically: if vy < 0 (jumping up) or otherFeet < platformTopY (still below
		// top)
		// Actually for one-way platforms: you only stand if you're coming from above.
		// So if otherFeet < platformTopY means other is below, we let pass.
		// If vy < 0 means it's moving upward, also let pass.
        if (vy < 0 || otherFeet < platformTopY) {
            return true;
        }

        return false;
    }

    private boolean isOnFloor(MotionComponent position, ColliderComponent collider, CollisionMapComponent map) {
		boolean isOnFloor = false;
        Rectangle solidBox = collider.getBoundingBox();
        solidBox.x += (int) position.getX();
        solidBox.y += (int) position.getY() + 2; // Controllo per il pavimento

        int entityBottomRow = (solidBox.y + solidBox.height) / map.getTileHeight();
        if (solidBox.y + solidBox.height < map.getMapHeight() * map.getTileHeight()) {
            int entityLeftCol = solidBox.x / map.getTileWidth();
            int entityRightCol = (solidBox.x + solidBox.width) / map.getTileWidth();
            for (int col = entityLeftCol; col <= entityRightCol; col++) {
                if (map.isSolidTile(entityBottomRow, col)) {
                    Rectangle rect = map.getSolidBox(entityBottomRow, col);
                    if (solidBox.intersects(rect)) {
                    	isOnFloor = true;
                    	map.setCollidedTile(entityBottomRow, col);
                    }
                }
            }
        } else {
        	isOnFloor = true;
        }
        return isOnFloor;
    }
    
    private void checkAndSetOnGround(Entity e, List<Entity> allEntities) {
        MotionComponent pos = e.getComponent(MotionComponent.class);
        ColliderComponent col = e.getComponent(ColliderComponent.class);
        GravityComponent grav = e.getComponent(GravityComponent.class);

        if (pos == null || col == null || grav == null) return;
        
        if (pos.getVy()<0) return;

        float feetY = pos.getY() + col.getBoundingBox().y + col.getBoundingBox().height;
        float checkY = feetY + 2; // check slightly below feet
        boolean onFloor = false;

	    CollisionMapComponent map = e.getComponent(CollisionMapComponent.class);
	    if (map != null)
	    	onFloor = isOnFloor(pos, col, map);

	    for (Entity other : allEntities) {
            if (other == e) continue;
            ColliderComponent oCol = other.getComponent(ColliderComponent.class);
            MotionComponent oPos = other.getComponent(MotionComponent.class);
            if (oCol == null || oPos == null) continue;

            if (oCol.getBehavior() == CollisionBehavior.STATIC) {
                float ox = oPos.getX() + oCol.getBoundingBox().x;
                float oy = oPos.getY() + oCol.getBoundingBox().y;
                float ow = oCol.getBoundingBox().width;
//                float oh = oCol.getBoundingBox().height;

                // Check horizontal overlap
                if (pos.getX() + col.getBoundingBox().width > ox && pos.getX() < ox + ow) {
                    // Check vertical position
                    float platformTopY = oy;
                    if (platformTopY >= feetY && platformTopY <= checkY) {
                        // For one-way platform, must come from above
                        if (!oCol.isOneWayPlatform() || feetY <= platformTopY) {
                            onFloor = true;
                            break;
                        }
                    }
                }
            }
        }

        // Set ground state in gravity component
        grav.setOnGround(onFloor);
        if (onFloor && grav.getAirSpeed() > 0) {
            grav.resetAirSpeed();
        }
    }	
}