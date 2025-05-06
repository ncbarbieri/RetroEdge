# Lezione 5: Rilevamento e risoluzione delle collisioni

## 1. Introduzione alle Collisioni

Il codice scritto fino a questo punto permette al personaggio di muoversi liberamente sulle tiles, ma la libertà d’azione del giocatore dev'essere limitata, per esempio per non farlo uscire fuori dalla mappa o per imporre un determinato cammino. Il passo successivo è quello di gestire le collisioni, per sapere quando il giocatore si scontra con un ostacolo e impedirne di conseguenza il movimento. Le collisioni sono fondamentali nei giochi per gestire interazioni realistiche tra personaggi, oggetti e ambienti. In questa lezione, ci concentriamo sulle collisioni entity-tilemap, usando l'algoritmo AABB.

## 2. Algoritmo AABB (Axis-Aligned Bounding Box)

L'algoritmo AABB è una tecnica efficiente per rilevare collisioni tra due rettangoli allineati agli assi cartesiani, cioè rettangoli che non ruotano. Ogni rettangolo è definito tramite:
- Coordinata **X** e **Y** (posizione dell'angolo superiore sinistro)
- Larghezza (**width**)
- Altezza (**height**)

Questo algoritmo sfrutta il fatto che per due rettangoli che non ruotano, la collisione può essere determinata rapidamente confrontando le loro coordinate sugli assi X e Y.

### 2.1 Condizioni di Collisione

Affinché si verifichi una collisione, i seguenti criteri devono essere contemporaneamente soddisfatti:
- Il lato destro del primo rettangolo deve essere più a destra del lato sinistro del secondo.
- Il lato sinistro del primo rettangolo deve essere più a sinistra del lato destro del secondo.
- Il lato inferiore del primo rettangolo deve essere più in basso del lato superiore del secondo.
- Il lato superiore del primo rettangolo deve essere più in alto del lato inferiore del secondo.

Espresso in pseudocodice Java:

```java
boolean intersects(Rect r1, Rect r2) {
    return r1.x < r2.x + r2.width &&
           r1.x + r1.width > r2.x &&
           r1.y < r2.y + r2.height &&
           r1.y + r1.height > r2.y;
}
```

Questo metodo garantisce rapidità ed efficienza, ideale per ambienti di gioco dove sono frequenti le verifiche di collisione.

## 3. ColliderComponent

Il ColliderComponent rappresenta la parte fisica di un’entità nel gioco, ovvero la sua capacità di entrare in collisione con altre entità o ostacoli. Questo componente consente di definire una zona di collisione, o "bounding box", per ciascuna entità, facilitando il rilevamento e la gestione delle interazioni tra entità diverse o tra entità e le tilemaps. È un componente ECS (Entity-Component-System) che fornisce:
- Bounding box (rettangolo di collisione) standard e orientata alla direzione
- Gestione delle collisioni (con flag colliding)
- Comportamenti associati alla collisione (via CollisionBehavior)
- Gestione della massa e delle piattaforme unidirezionali (es. salti attraverso piattaforme da sotto)
- Effetti visivi temporanei delle collisioni

### 3.1 Struttura interna e campi principali

| Campo	                    | Tipo	                    | Descrizione                                                                             |
|---------------------------|---------------------------|-----------------------------------------------------------------------------------------|
| defaultBoundingBox	    | Rectangle	                | Bounding box predefinita (senza direzione)                                              |
| directionalBoundingBoxes	| Map<Direction, Rectangle>	| Bounding box specifiche a seconda della direzione                                       |
| boundingBox	            | Rectangle              	| Bounding box attiva (clonata da default o da una direzione)                    |
| behavior	                | CollisionBehavior	        | Definisce il comportamento durante la collisione (es. rimbalzo, blocco, passaggio)|
| mass	                    | float	                    | Massa dell’entità, utile per simulazioni fisiche                                        |
| oneWayPlatform	        | boolean	                | Se true, è una piattaforma attraversabile da sotto (solo per entità statiche)         |
| colliding	                | boolean	                | Indica se una collisione è avvenuta di recente                                          |
| timeRemaining             | float	                    | Tempo residuo per mostrare un effetto visivo della collisione                           |

### 3.2 Costruttori

1.	Posizione e dimensioni fisse:
   
    `new ColliderComponent(entity, x, y, width, height, behavior);`
3.	Bounding box come oggetto Rectangle:

    `new ColliderComponent(entity, boundingBox, behavior);`
4.	Bounding box derivata dalla definizione dello spritesheet:

    `new ColliderComponent(entity, spritesheet, behavior);`

Questo approccio multiplo consente flessibilità per entità con bounding box dinamici (es. personaggi animati) o statici (es. muri).

### 3.3 Metodi chiave

- **collides()**: segnala che una collisione è avvenuta e fa partire il timer per l’effetto visivo di debug.
- **update(deltaTime)**: gestisce la disattivazione dell’effetto visivo dopo un tempo (COLLISION_VISUAL_DURATION).
- **setDefaultBoundingBox(Direction)**: aggiorna il bounding box in base alla direzione corrente (utile per personaggi che cambiano forma).
- **setBoundingBox(Direction, Rectangle)**: imposta un bounding box per una direzione specifica.

### 3.4 Accessori e modificatori

- **getBoundingBox()** restituisce una copia clonata del bounding box attivo → importante per evitare modifiche involontarie da parte di altri sistemi.
- **getBehavior(), getMass(), isOneWayPlatform()** offrono accesso alle proprietà fisiche e comportamentali.
- **setMass(float)** permette di impostare la massa dinamicamente.

### 3.5 Interazione con altri sistemi

Questo componente viene tipicamente interrogato dal sistema delle collisioni (CollisionSystem), che:
1.	Rileva sovrapposizioni di bounding box tra entità.
2.	Chiama collides() per impostare il flag e attivare eventuali effetti visivi di debug.
3.	Usa getBehavior() per determinare la risposta alla collisione (blocco, passaggio, rimbalzo ecc.).

### 3.6 Ulteriori osservazioni

- La presenza di bounding box direzionali rende questo componente estremamente flessibile, utile ad esempio per gestire animazioni (camminata, salto, ecc.) in cui il collider cambia forma.
- L’uso di clone() per le Rectangle impedisce effetti collaterali, ma può avere costi computazionali se fatto in un ciclo di gioco ad alta frequenza.
- Il campo colliding viene usato per evidenziare l’entità o per effetti audio/visivi nel rendering system.

## 4. Rilevamento delle Collisioni

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

## 5. Risoluzione delle Collisioni

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

## 6. Implementare CollisionBehavior

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
