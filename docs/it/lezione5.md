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

### 3.1 Struttura interna e attributi principali

| Attributo	                | Tipo	                    | Descrizione                                                                             |
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

### 3.3 Metodi principali

- **collides()**: segnala che una collisione è avvenuta e fa partire il timer per l’effetto visivo di debug.
- **update(deltaTime)**: gestisce la disattivazione dell’effetto visivo dopo un tempo (COLLISION_VISUAL_DURATION).
- **setDefaultBoundingBox(Direction)**: aggiorna il bounding box in base alla direzione corrente (utile per personaggi che cambiano forma).
- **setBoundingBox(Direction, Rectangle)**: imposta un bounding box per una direzione specifica.
- **getBoundingBox()** restituisce una copia clonata del bounding box attivo → importante per evitare modifiche involontarie da parte di altri sistemi.
- **getBehavior(), getMass(), isOneWayPlatform()** offrono accesso alle proprietà fisiche e comportamentali.
- **setMass(float)** permette di impostare la massa dinamicamente.

### 3.4 Interazione con altri sistemi

Questo componente viene tipicamente interrogato dal sistema delle collisioni (CollisionSystem), che:
1.	Rileva sovrapposizioni di bounding box tra entità.
2.	Chiama collides() per impostare il flag e attivare eventuali effetti visivi di debug.
3.	Usa getBehavior() per determinare la risposta alla collisione (blocco, passaggio, rimbalzo ecc.).

### 3.5 Ulteriori osservazioni

- La presenza di bounding box direzionali rende questo componente estremamente flessibile, utile ad esempio per gestire animazioni (camminata, salto, ecc.) o direzioni in cui il collider cambia forma.
- L’uso di clone() per le Rectangle impedisce effetti collaterali, ma può avere costi computazionali se fatto in un ciclo di gioco ad alta frequenza.
- Il campo colliding viene usato per evidenziare l’entità o per effetti audio/visivi nel rendering system.

Ottima precisazione, Christian! Allora aggiorno l’analisi tenendo conto di questo importante dettaglio d’implementazione: in Retro Edge, l’enum CollisionBehavior non definisce se l’entità si muove, ma se è influenzata dalle collisioni con altri oggetti. Questo cambia un po’ la prospettiva didattica.

## 4. CollisionBehavior

L’enum CollisionBehavior specifica il comportamento di un’entità rispetto alle collisioni, ovvero:
- Se viene influenzata da urti con altre entità dinamiche
- Se può spostarsi in autonomia, ma non deve essere spostata da altri

In altre parole: non indica se l’entità si muove, ma chi cede e chi no in una collisione.

| Valore	| Descrizione operativa   |
|-----------|-------------------------|
| STATIC	| non subisce spostamenti causati da collisioni (ma può benissimo muoversi da solo, come nel caso di un nemico che cammina) |
| DYNAMIC	| viene spinto in caso di urto, che sia con un oggetto fisso o con un altro dinamico |

Esempi pratici:

| Entità	| Movimento	| Comportamento di collisione	| Valore  |
|-----------|-----------|-------------------------------|---------------|
| Muro	| No	| Blocco fisso	| STATIC |
| Suolo	| No	| Blocco fisso	| STATIC |
| Giocatore	| Sì	| Può essere spinto	| DYNAMIC |
| Proiettile	| Sì	| Reagisce a urti | DYNAMIC |
| Cassa spostabile	| Sì	| Spinta dal giocatore	| DYNAMIC |
| Nemico	| Sì	| Non viene spinto dal giocatore	| STATIC |
| NPC	| Sì	| Cammina, ma non reagisce alla spinta	| STATIC |

Perché STATIC anche per NPC o nemici?

Perché STATIC significa “non reattivo alle collisioni”, non “immobile”. Il movimento di tali entità è gestito dallo script o dall’IA, non da forze esterne, quindi: 
- Non devono essere respinti dal giocatore.
- Non devono “scivolare via” dopo un contatto.
- Possono comunque camminare, saltare, inseguire, ecc.


## 5. CollisionMapComponent

Il CollisionMapComponent rappresenta il sistema di collisione per mappe tile-based, ovvero ambienti composti da celle (o tile) quadrate o rettangolari, come nei giochi platform o RPG 2D. Mentre ColliderComponent rappresenta il “**cosa può entrare in collisione**”, CollisionMapComponent rappresenta il “**dove può entrare in collisione**”. Serve a:
- Identificare le tile solide (che bloccano il movimento)
- Gestire l’evidenziazione temporanea delle collisioni
- Offrire supporto al sistema di collisione globale nel gioco

### 5.1 Struttura interna e attributi principali
| Attributo                 | Tipo	                    | Descrizione                                                                             |
|---------------------------|---------------------------|----------------------------------------------------------------------------------------------------------------|
| solidTiles	            | `boolean[][]`	            | Mappa bidimensionale che indica se una tile è solida (true) o attraversabile (false)                   |
| tileWidth, tileHeight	    | `int`                     | Dimensioni in pixel delle singole tile                                                                         |
| collidedTiles	            | `Map<Point, Float>`       | Mappa che tiene traccia delle tile in cui è avvenuta una collisione e della durata residua per effetti visivi  |

### 5.2 Costruttore

`public CollisionMapComponent(Entity entity, TileMap tileMap)`

- Recupera le dimensioni delle tile dalla TileMap
- Ottiene la mappa delle tile solide (solidTiles) e la mappa delle tile che sono entrate in collisione (collidedTiles) dalla TileMap

Il collidedTiles viene condiviso dalla TileMap per permettere a più sistemi di accedere a questa struttura per visualizzare o gestire lo stato delle collisioni in tempo reale (per esempio il RenderingSystem per visualizzare le informazioni di debug). Questo approccio centralizza lo stato delle collisioni sulle tile, utile per il rendering di effetti visivi o per la logica di gameplay condivisa (es. tile che diventano temporaneamente inattive dopo una collisione).

### 5.3 Metodi principali

`isSolidTile(int row, int col)`

Controlla se una data tile (riga, colonna) è solida:
- Verifica che gli indici siano validi
- Restituisce true se la tile è marcata come solida nella matrice solidTiles

Utile quando si vuole determinare se il movimento di un’entità porterà a una collisione con l’ambiente.

`update(float deltaTime)`

Aggiorna i timer associati alle tile in collisione:
- Riduce il tempo rimanente per ciascuna tile
- Rimuove automaticamente le tile scadute dalla mappa collidedTiles

Questo permette di realizzare effetti visivi a tempo (es. lampeggi, animazioni di urto).

`setCollidedTile(int row, int col)`

Registra che una collisione è avvenuta su una tile:
- Crea un Point con la posizione (colonna, riga)
- Aggiunge o aggiorna quella entry nella mappa collidedTiles con un timer predefinito (COLLISION_VISUAL_DURATION)

### 5.4 Metodi principali

| Metodo	                      | Descrizione                                                                                                         |
|---------------------------------|---------------------------------------------------------------------------------------------------------------------|
| getTileWidth(), getTileHeight() |	Restituiscono la dimensione delle tile in pixel                                                                     |
| getMapWidth(), getMapHeight()	  | Forniscono la dimensione logica della mappa (solidTiles[0].length e solidTiles.length)                              |
| getSolidBox(int row, int col)	  | Restituisce un Rectangle con le coordinate della tile (utile per disegnarla o controllare collisioni pixel-based) |
| getSolidTiles()	              | Espone la matrice booleana delle tile solide (solo per lettura)                                                     |

## 6. CollisionSystem: Analisi e Funzione

Il CollisionSystem è il cuore della gestione delle collisioni. Ha il compito di:
1.	Rilevare e risolvere le collisioni tra entità e tile.
2.	Rilevare e risolvere le collisioni tra entità dinamiche.
3.	Aggiornare lo stato di “a terra” (onGround) per la fisica e la gravità.
4.	Applicare effetti visivi e restituzione (rimbalzo) alle entità coinvolte.

È un sistema prioritario (super(engine, 4)) che agisce solo in determinati stati del motore (es. non in pausa o durante cutscene).

### 6.1 Ciclo di aggiornamento
Il metodo update del sistema compie le seguenti operazioni:
1.	Collisioni con tile (prima passata):
    Controlla se le entità dinamiche collidono con tile solide, e le riposiziona di conseguenza.
2.	Collisioni tra entità:
    Verifica sovrapposizioni tra bounding box di tutte le coppie di entità e gestisce il rimbalzo, il blocco e l’interazione.
3.	Collisioni con tile (seconda passata):
    Serve per correggere eventuali effetti collaterali della collisione tra entità.
4.	Aggiornamento stato “onGround”:
    Indica se un’entità è poggiata su una superficie, fondamentale per il comportamento della gravità.

### 6.2 Componenti coinvolti

| Componente	| Scopo  |
|---------------|--------|
| MotionComponent	| Tiene traccia della posizione e della velocità dell’entità |
| ColliderComponent	| Fornisce la bounding box e il comportamento fisico (STATIC o DYNAMIC) |
| CollisionMapComponent	| Rappresenta la mappa delle tile solide |
| GravityComponent	| Gestisce lo stato a terra e la caduta |
| InteractionComponent	| Tiene traccia delle entità o tile con cui è avvenuto un contatto |

### 6.3 Funzionalità principali

**Tile Collision**

Metodo: resolveTileCollisions(...)
- Verifica collisioni tra l’entità e le tile usando findCollisionsInRect(...)
- Risolve prima la componente orizzontale e poi quella verticale, correggendo la posizione con una ricerca binaria per ottenere la posizione libera più vicina (binarySearchFreePositionX/Y)
- Blocca la velocità nella direzione della collisione

La correzione avviene solo se c’è una collisione con tile solide (muri, pavimenti, ecc.)

**Entity Collision**

Metodo: handleCollision(...)
- Confronta tutte le coppie di entità
- Se le bounding box si sovrappongono:
   - Aggiunge le entità all’InteractionComponent (per usi come dialoghi, pickup, danni)
   - Risolve la collisione in base al CollisionBehavior
      - STATIC vs DYNAMIC: solo l’entità dinamica viene spostata
      - DYNAMIC vs DYNAMIC: entrambe si spostano in proporzione alla massa
   - Applica restituzione (rimbalzo) se configurata

**Restituzione (rimbalzo)**

Metodo: applyRestitution(...)
- Applica un cambiamento di velocità lungo la normale della collisione
- L’energia restituita è determinata da restitution (valore configurabile)

**Piattaforme unidirezionali**

Metodo: shouldPassThrough(...)

Permette di passare attraverso una piattaforma STATIC se:
- L’entità si sta muovendo verso l’alto (es. salto da sotto)
- L’entità non ha ancora toccato il bordo superiore della piattaforma

**Controllo del contatto col terreno**

Metodo: checkAndSetOnGround(...)
- Determina se un’entità è “a terra”, cioè in contatto con una tile solida o un’altra entità con STATIC
- Essenziale per fermare la gravità e permettere il salto
- Utilizza isOnFloor(...) per controllare una piccola area sotto i piedi dell’entità

**Differenza tra STATIC e DYNAMIC**
| Tipo di Collisione	| Azione del CollisionSystem	| Esempio |
|-----------------------|-------------------------------|---------|
| STATIC vs STATIC	| Ignorata	| Due muri, due NPC |
| STATIC vs DYNAMIC	| La DYNAMIC viene spostata	| Giocatore che urta un nemico |
| DYNAMIC vs STATIC	| La DYNAMIC viene spostata	| Proiettile che colpisce una parete |
| DYNAMIC vs DYNAMIC	| Entrambe si spostano in proporzione alla massa	| Due casse che si spingono |

Lo scontro tra due entità STATIC non viene gestito intenzionalmente. Questo comportamento si fonda su una buona progettazione dei livelli e delle entità, secondo cui:
- Due entità con comportamento STATIC non dovrebbero mai trovarsi nella stessa posizione.
- Il CollisionSystem ignora il caso STATIC vs STATIC, per motivi di performance e coerenza logica.

Nota: Le collisioni tra due entità STATIC non vengono gestite dal sistema. Se dovessero verificarsi, è segno di un errore di progettazione. Assicurati che nemici, NPC o muri non si sovrappongano tra loro nella fase di costruzione della mappa.

## 7. Esempio d'uso

Durante l’inizializzazione dello stato, configuriamo il giocatore con i componenti di collisione e aggiungiamo il sistema globale. Facciamo attenzione alla posizione iniziale del giocatore: se è in collisione con una tile, non potrà muoversi.
```java
@Override
public void init() {
    // 1. Creazione mondo
    Entity tileManager = new Entity(EntityType.TILEMANAGER, 1);
    TileMap world = new TileMap("/tiles/tileset.png", "/maps/world.txt", 0.16f);
    TileMapComponent tileMapComponent = new TileMapComponent(tileManager, world);
    tileManager.addComponent(tileMapComponent);
    add(tileManager);

    // 2. Creazione entità giocatore
    Entity player = new Entity(EntityType.PLAYER, 2);
    CharacterSpritesheet playerSpritesheet = new CharacterSpritesheet("/sprites/player.sprite");
    MotionComponent playerPosition = new MotionComponent(player, 130, 130, 300.0f);
    player.addComponent(playerPosition);
    KeyInputComponent playerInput = new KeyInputComponent(player);
    player.addComponent(playerInput);
    SpriteComponent playerSprites = new SpriteComponent(player, playerSpritesheet, .08f, true);
    player.addComponent(playerSprites);

    // 3. Componenti di collisione
    ColliderComponent playerCollider = new ColliderComponent(
        player,
        playerSpritesheet.getBoundingBox(),
        CollisionBehavior.DYNAMIC
    );
    player.addComponent(playerCollider);

    CollisionMapComponent playerCollisionMap = new CollisionMapComponent(
        player,
        world // world è la TileMap corrente
    );
    player.addComponent(playerCollisionMap);

    // 4. Registrazione del player nel motore
    engine.addEntity(player);

    // 5. Creazione della camera:
    Camera camera = (Camera) new FollowPlayer(world, player);

    // 6. Aggiunta dei sistemi al motore
    InputSystem inputSystem = new InputSystem(this.engine);
    inputSystem.bindAction(InputAction.MOVE_LEFT, KeyEvent.VK_A);
    inputSystem.bindAction(InputAction.MOVE_RIGHT, KeyEvent.VK_D);
    inputSystem.bindAction(InputAction.MOVE_UP, KeyEvent.VK_W);
    inputSystem.bindAction(InputAction.MOVE_DOWN, KeyEvent.VK_S);
    inputSystem.bindCustomAction(InputAction.PAUSE, KeyEvent.VK_P, deltaTime -> {
        EngineState currentState = engine.getStateManager().getCurrentState();
        if (currentState == EngineState.RUNNING) {
            engine.getStateManager().requestStateChange(EngineState.PAUSED);
        } else if (currentState == EngineState.PAUSED) {
            engine.getStateManager().requestStateChange(EngineState.RUNNING);
        }
    });
    inputSystem.addDebouncedAction(InputAction.PAUSE);
    inputSystem.bindCustomAction(InputAction.DEBUG, KeyEvent.VK_0, deltaTime -> {
        if (engine.isDebug()) {
            engine.setDebug(false);
        } else {
            GamePanel.resetDebug();
            engine.setDebug(true);
        }
    });
    inputSystem.addDebouncedAction(InputAction.DEBUG);
    add(inputSystem);
    add(new MotionSystem(this.engine));
    add(new AnimationSystem(this.engine));
    add(new RenderingSystem(this.engine, camera));

    // 7. Aggiunta del CollisionSystem
    add(new CollisionSystem(this.engine));

}
```
