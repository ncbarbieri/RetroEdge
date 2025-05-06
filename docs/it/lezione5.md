**Lezione 5: Rilevamento e risoluzione delle collisioni tra entity e tilemap**

### 1. Introduzione alle Collisioni

Le collisioni sono fondamentali nei giochi per gestire interazioni realistiche tra personaggi, oggetti e ambienti. In questa lezione, ci concentriamo sulle collisioni entity-tilemap, usando l'algoritmo AABB.

### 2. Algoritmo AABB (Axis-Aligned Bounding Box)

L'algoritmo AABB rileva collisioni tra due rettangoli allineati agli assi cartesiani, ovvero rettangoli che non ruotano.

### Condizione di Collisione

Per determinare se due rettangoli si intersecano, devono verificarsi contemporaneamente queste condizioni:

```java
boolean intersects(Rect r1, Rect r2) {
    return r1.x < r2.x + r2.width &&
           r1.x + r1.width > r2.x &&
           r1.y < r2.y + r2.height &&
           r1.y + r1.height > r2.y;
}
```

### 3. Le classi coinvolte

* **CollisionSystem.java**: gestisce la logica delle collisioni.
* **ColliderComponent.java**: componente associato ad ogni entity che può entrare in collisione.
* **CollisionMapComponent.java**: rappresenta la tilemap utilizzata per le collisioni.
* **CollisionBehavior.java**: definisce il comportamento da attuare al verificarsi di una collisione.

### 4. Rilevamento delle Collisioni

Per ogni entity che possiede un `ColliderComponent`, il `CollisionSystem` verifica eventuali collisioni con la `CollisionMapComponent` utilizzando l'algoritmo AABB.

```java
for (ColliderComponent collider : colliders) {
    Rectangle entityBounds = collider.getBounds();

    for (Rectangle tile : collisionMap.getCollisionTiles()) {
        if (intersects(entityBounds, tile)) {
            resolveCollision(collider, tile);
        }
    }
}
```

### 5. Risoluzione delle Collisioni

La risoluzione della collisione consiste nello spostare l'entity nella posizione più vicina in cui i rettangoli non si intersecano:

```java
void resolveCollision(ColliderComponent entityCollider, Rectangle tile) {
    Rectangle entityBounds = entityCollider.getBounds();

    float overlapX = Math.min(
        entityBounds.x + entityBounds.width - tile.x,
        tile.x + tile.width - entityBounds.x);

    float overlapY = Math.min(
        entityBounds.y + entityBounds.height - tile.y,
        tile.y + tile.height - entityBounds.y);

    if (overlapX < overlapY) {
        entityCollider.entity.transform.position.x += (entityBounds.x < tile.x) ? -overlapX : overlapX;
    } else {
        entityCollider.entity.transform.position.y += (entityBounds.y < tile.y) ? -overlapY : overlapY;
    }

    entityCollider.onCollision(tile);
}
```

### 6. Implementare CollisionBehavior

Il comportamento post-collisione viene definito tramite `CollisionBehavior`. Un esempio:

```java
public class BounceBehavior implements CollisionBehavior {
    @Override
    public void onCollision(Entity entity, Rectangle collider) {
        // esempio: inversione della velocità
        entity.velocity.x = -entity.velocity.x;
        entity.velocity.y = -entity.velocity.y;
    }
}
```
