package engine.systems;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import engine.BaseSystem;
import engine.Engine;
import engine.components.SpriteComponent;
import engine.components.TileMapComponent;
import engine.Entity;
import enums.EngineState;
import pathfinder.Node;
import pathfinder.PathFinder;
import world.Camera;
import world.TileMap;
import engine.components.AttackComponent;
import engine.components.ChaseComponent;
import engine.components.ColliderComponent;
import engine.components.CollisionMapComponent;
import engine.components.MotionComponent;
import engine.components.ParallaxComponent;

public class RenderingSystem extends BaseSystem {
	private final Comparator<Entity> entityComparator;
	private Camera camera;
	private int currentXOffset;
	private int currentYOffset;
//    private static final Color collisionColor = new Color(1f, 1f, 0.1f, .5f);
	private static final Color solidColor = new Color(1f, 0f, 0f, .5f);

	// Costruttore con Camera
	public RenderingSystem(Engine engine, Camera camera) {
		super(engine, 10);
		this.camera = camera;
		this.entityComparator = createEntityComparator();
	}

	@Override
	protected void initStateUpdateMap() {
		// Customize states where this system updates
		setUpdateInState(EngineState.STARTING, false);
		setUpdateInState(EngineState.RUNNING, true);
		setUpdateInState(EngineState.CUTSCENE, true);
		setUpdateInState(EngineState.PAUSED, true);
		setUpdateInState(EngineState.SHOWING_DIALOG, true);
		setUpdateInState(EngineState.EXITING, true);
		setUpdateInState(EngineState.ENTERING, true);
	}

	// Costruttore senza Camera
	public RenderingSystem(Engine engine) {
		this(engine, null);
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void update(float deltaTime) {
		// Aggiorna le collisioni tra entità e tile
		engine.accessEntities(entities -> {
			for (Entity entity : entities) {
				updateEntity(entity, deltaTime);
			}
		});

		if (camera != null) {
			camera.update(deltaTime);
		}
	}

	@Override
	protected void updateEntity(Entity entity, float deltaTime) {
		if (engine.isDebug()) {
			if (entity.hasComponent(CollisionMapComponent.class)) {
				CollisionMapComponent collisionMap = entity.getComponent(CollisionMapComponent.class);
				collisionMap.update(deltaTime);
			}
			if (entity.hasComponent(ColliderComponent.class)) {
				ColliderComponent colliderComponent = entity.getComponent(ColliderComponent.class);
				colliderComponent.update(deltaTime);
			}
		}
	}

	@Override
	public void render(Graphics2D g) {
		currentXOffset = (camera != null) ? camera.getxOffset() : 0;
		currentYOffset = (camera != null) ? camera.getyOffset() : 0;

		engine.sortEntities(entityComparator);

		engine.accessEntities(entities -> {
			for (Entity entity : entities) {
				renderEntity(entity, g);
			}
		});
	}

	protected void renderEntity(Entity entity, Graphics2D g) {
		MotionComponent pc = entity.getComponent(MotionComponent.class);
		SpriteComponent sc = entity.getComponent(SpriteComponent.class);

		if (sc != null && pc != null) {
			g.drawImage(sc.getCurrentSprite(), (int) (pc.getX() - currentXOffset), (int) (pc.getY() - currentYOffset),
					null);
		}

		if (engine.isDebug() && pc != null) {
			ColliderComponent cc = entity.getComponent(ColliderComponent.class);
			if (cc != null) {
				g.setColor(new java.awt.Color(1f, 0f, 0f, 0.5f));
				Rectangle boundingBox = cc.getBoundingBox();
				g.fillRect((int) (pc.getX() + boundingBox.x - currentXOffset),
						(int) (pc.getY() + boundingBox.y - currentYOffset), boundingBox.width, boundingBox.height);
				if (cc.isColliding()) {

					// Calcola l'alpha in base al tempo rimanente
					float alpha = cc.getTimeRemaining() / CollisionMapComponent.COLLISION_VISUAL_DURATION;
					g.setColor(new Color(1f, 1f, 0.1f, alpha)); // Giallo trasparente proporzionale al timer
					g.fillRect((int) (pc.getX() + boundingBox.x - currentXOffset),
							(int) (pc.getY() + boundingBox.y - currentYOffset), boundingBox.width, boundingBox.height);
				}
			}
			
			AttackComponent ac = entity.getComponent(AttackComponent.class);
			if (ac != null && ac.isAttacking()) {
				Rectangle baseHitBox = ac.getHitBox(sc.getDirection().getDirectionIndex());
				if (baseHitBox != null) {
					Rectangle hitBox = (Rectangle) baseHitBox.clone();
					hitBox.x += (int) pc.getX();
					hitBox.y += (int) pc.getY();
					g.setColor(new Color(0f, 1f, 0f, .5f));
					g.fillRect(hitBox.x - currentXOffset, hitBox.y - currentYOffset, hitBox.width, hitBox.height);
				}
			}
		}

		if (entity.hasComponent(TileMapComponent.class)) {
			// Disegno la mappa con l'offset della camera
			TileMapComponent tileMapComponent = entity.getComponent(TileMapComponent.class);
			BufferedImage tileMapImage = tileMapComponent.getTileMapImage();
			g.drawImage(tileMapImage, -currentXOffset, -currentYOffset, null);
			if (engine.isDebug()) {
				int startRow = currentYOffset / tileMapComponent.getTileHeight();
				int startCol = currentXOffset / tileMapComponent.getTileWidth();
				for (int row = Math.max(0, startRow); row < Math.min(startRow + tileMapComponent.getVisibleRows() + 1,
						tileMapComponent.getMapHeight()); row++) {
					for (int col = Math.max(0, startCol); col < Math.min(
							startCol + tileMapComponent.getVisibleCols() + 1, tileMapComponent.getMapWidth()); col++) {
						if (tileMapComponent.isSolidTile(row, col)) {
							Rectangle solidBox = tileMapComponent.getSolidBox(row, col);
							g.setColor(solidColor);
							g.fillRect(solidBox.x - currentXOffset, solidBox.y - currentYOffset, solidBox.width,
									solidBox.height);
						}
					}
				}
				
				Map<Point, Float> collidedTiles = tileMapComponent.getCollidedTiles();
				Iterator<Map.Entry<Point, Float>> iterator = collidedTiles.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<Point, Float> entry = iterator.next();
					Point tilePoint = entry.getKey(); // La posizione della tile in coordinate di griglia
					Float timeRemaining = entry.getValue(); // Il tempo residuo per la trasparenza

					// Calcola la posizione sullo schermo della tile
					int x = tilePoint.x * tileMapComponent.getTileWidth() - currentXOffset;
					int y = tilePoint.y * tileMapComponent.getTileHeight() - currentYOffset;

					// Calcola l'alpha in base al tempo rimanente
					float alpha = timeRemaining / CollisionMapComponent.COLLISION_VISUAL_DURATION;
					g.setColor(new Color(1f, 1f, 0.1f, alpha)); // Giallo trasparente proporzionale al timer

					// Disegna la tile
					g.fillRect(x, y, tileMapComponent.getTileWidth(), tileMapComponent.getTileHeight());
				}
			}
		}
		
        if (entity.hasComponent(ParallaxComponent.class)) {
            ParallaxComponent parallax = entity.getComponent(ParallaxComponent.class);
            BufferedImage image = parallax.getImage();
            float speed = parallax.getSpeed();
            int yPosition = parallax.getYPosition();
            int imageWidth = image.getWidth();
            int worldWidth = parallax.getWorldWidth();
            int startX = -(int) (currentXOffset * speed) % imageWidth;
            if (startX > 0) startX -= imageWidth;

            for (int x = startX; x < worldWidth; x += imageWidth) {
                g.drawImage(image, x, yPosition, null);
            }
        }

		if (engine.isDebug()) {
			if (entity.hasComponent(ChaseComponent.class)) {
				ChaseComponent chaseComponent = entity.getComponent(ChaseComponent.class);
				PathFinder pathFinder = chaseComponent.getPathfinder();
				if (pathFinder != null) {
					TileMap tileManager = chaseComponent.getTileManager();
					int tileWidth = tileManager.getTileWidth();
					int tileHeight = tileManager.getTileHeight();

					g.setColor(new Color(0f, 1f, 1f, 0.5f));
					List<Node> path = pathFinder.getPath();
					if (path != null) {
						for (Node node : path) {
							g.fillRect(node.x * tileWidth - currentXOffset, node.y * tileHeight - currentYOffset, tileWidth,
									tileHeight);
						}
					}
				}
			}
		}
	}

	private Comparator<Entity> createEntityComparator() {
		return (e1, e2) -> {
			if (e1.getLayer() != e2.getLayer()) {
				return Integer.compare(e1.getLayer(), e2.getLayer());
			}
			MotionComponent p1 = e1.getComponent(MotionComponent.class);
			MotionComponent p2 = e2.getComponent(MotionComponent.class);
			ColliderComponent c1 = e1.getComponent(ColliderComponent.class);
			ColliderComponent c2 = e2.getComponent(ColliderComponent.class);

			if (p1 != null && p2 != null) {
				float y1 = p1.getY() + (c1 != null ? c1.getBoundingBox().y + c1.getBoundingBox().height : 0);
				float y2 = p2.getY() + (c2 != null ? c2.getBoundingBox().y + c2.getBoundingBox().height : 0);
				return Float.compare(y1, y2);
			}

			return 0;
		};
	}

}