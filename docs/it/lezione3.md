# Lezione 3 – Sprite, Movimento e Animazione

## Obiettivi
- Comprendere come caricare e gestire **sprite** per creare l’animazione del personaggio giocante.  
- Approfondire i **sistemi** di movimento, input, animazione e rendering.  
- Introdurre il **metodo di Eulero semi-implicito** per aggiornare posizione e velocità in modo “realistico”.  
- Mostrare come avviene il **rendering** delle entità animabili.

---

## Introduzione
Finora abbiamo costruito l’**infrastruttura** del gioco con il pattern ECS: entità, componenti, sistemi. Adesso passiamo a **movimento** e **animazione**:  
- **Input**: gestire i comandi impartiti dall'utente.  
- **Animazione**: gestire una sequenza di immagini (frame) che si susseguono per dare l’illusione del movimento.  
- **Movimento** “realistico”: aggiornare posizione e velocità di un’entità in base a forze, accelerazioni e input del giocatore.  
- **Rendering**: disegnare il frame corrente dell’animazione sullo schermo, nella posizione corretta.

Quattro sistemi principali ci aiuteranno:
1. **`InputSystem`** e **`KeyInputComponent`**: interpreta i comandi dell’utente (tastiera, mouse, gamepad…).  
2. **`MotionSystem`** e **`MotionComponent`**: calcola e applica gli aggiornamenti di posizione usando Eulero semi-implicito.  
3. **`AnimationSystem`** e **`SpriteComponent`**: gestisce i cicli di animazione, passando da un frame all’altro.  
4. **`RenderingSystem`**: disegna effettivamente le entità sullo schermo con le coordinate e i frame adeguati.

---

## 1. Sprite Sheet e Animazioni

Con [sprite](https://it.wikipedia.org/wiki/Sprite_(informatica)), in informatica, si indica una bitmap, un’immagine raster composta da pixel e generalmente 2D, che fa parte di una scena più grande (lo "sfondo") e che può essere spostata in maniera indipendente rispetto ad essa. Può essere sia statica, sia dinamica. Gli sprite furono inventati per gestire in maniera rapida ed efficiente speciali porzioni di grafica nei videogiochi usando hardware appositamente dedicato, poiché allora le CPU non erano sufficientemente potenti per compiere tali operazioni. Il Texas Instruments TMS9918 è il primo chip video nella cui documentazione viene per la prima volta utilizzato ufficialmente il termine "sprite" per indicare questi oggetti grafici. Secondo Karl Guttag, uno dei due ingegneri che sviluppò il TMS9918, il termine deriva dal fatto che i dati degli sprites, piuttosto che far parte della stessa area di memoria video dello sfondo, ci fluttuano sopra da un’altra area di memoria senza alterarne il contenuto, come un fantasma o uno spirito mitologico.

 Uno sprite sheet è un’unica immagine che contiene **più frame** di animazione. Ad esempio, potremmo avere un file PNG con tutte le pose del personaggio: idle, camminata, salto, ecc., disposte in griglia. Per disegnare il frame corrente, estraiamo (o “clippiamo”) la porzione di immagine corrispondente alla posa desiderata e la stampiamo sullo schermo. Per creare uno spritesheet, si può usare l’applicazione open source [Universal LPC Spritesheet Generator](https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/). Si possono trovare anche risorse on line, per esempio ai seguenti siti:
- [Open Art](https://opengameart.org/)
- [CraftPix](https://craftpix.net/categorys/2d-game-kits/)

### 1.1 Definizione dello Sprite Sheet

Un **sprite sheet** è un’unica risorsa grafica (un file `.png` o `.jpg`) che contiene più **frame** di animazione. Nel nostro progetto, diamo anche la possibilità di definire un file `.sprite` che descrive:
1. **Il nome del file immagine** (es. `fileName:/spritesheet/player.png`).  
2. **Le dimensioni** di ogni frame (`frameWidth`, `frameHeight`).  
3. **Bounding box** di default, usata per collisioni o calcolo di offset (`boundingBox:58-58-28-28`).  
4. **Azioni** (`action`) con la lista dei frame:  
   - Esempio: `action:IDLE:DOWN:8-0` significa che per l’azione `IDLE`, direzione `DOWN`, usiamo i frame sulla **riga 8** e **colonna 0**.  
   - Se abbiamo una sequenza, la sintassi `action:ATTACK:RIGHT:0-0,0-1,0-2,...` elenca i vari frame da disegnare in quell’ordine.

### 1.2 Esempio di file `.sprite`
Ecco una sezione di esempio:

```
fileName:/spritesheet/player.png
frameWidth:144
frameHeight:144
boundingBox:58-58-28-28
action:IDLE:DOWN:8-0
action:IDLE:LEFT:8-1
action:IDLE:UP:8-2
action:IDLE:RIGHT:8-3
action:ATTACK:RIGHT:0-0,0-1,0-2,0-3,0-4,0-5,0-6,0-7,0-8,0-9,0-10,0-11
…
action:WALK:DOWN:7-0,7-1,7-2,7-3,7-4,7-5,7-6,7-7
action:FALL:DOWN:9-0,9-1,9-2
```

- `fileName:/spritesheet/player.png`: indica il percorso dell’immagine con tutti i frame del player.  
- `frameWidth:144` e `frameHeight:144`: ogni frame è un quadrato 144×144 pixel.  
- `boundingBox:58-58-28-28`: un box interno all’immagine, spesso usato per la collisione o per definire il “centro” del personaggio.  
- `action:...` definisce diverse animazioni.  
  - Esempio: `action:ATTACK:RIGHT:0-0,0-1,0-2...` significa che per l’azione di “ATTACK” in direzione “RIGHT”, useremo i frame (riga=0, colonna=0), (riga=0, colonna=1), (riga=0, colonna=2) e così via.

**Nota**: la riga e la colonna indicano in quale sezione dello sprite sheet trovare quel frame. Se `frameWidth = 144`, allora la **colonna 0** è i pixel `(0..143)` in orizzontale, la colonna 1 è `(144..287)` e così via.

---

### 1.3. La classe `Spritesheet.java`
Questa classe tipicamente:
1. **Legge** il file `.sprite` (come quello sopra).  
2. **Carica** la risorsa immagine (`player.png`) e la memorizza in un `BufferedImage`.  
3. **Memorizza** le informazioni di bounding box, dimensioni del frame e l’elenco delle azioni.  

La struttura della classe è la seguente:

```java
public class Spritesheet {
    private BufferedImage spriteImage;
    private int frameWidth;
    private int frameHeight;
    private Rectangle boundingBox;
    // Mappa da actionName -> direction -> lista di frame
    private Map<String, Map<String, List<FrameIndex>>> actions;

    public Spritesheet(String spriteFilePath) {
        // 1) Legge il file .sprite riga per riga
        // 2) In base al prefisso (fileName:, frameWidth:, ecc.) salva i dati
        // 3) Carica l'immagine con ImageIO.read(...)
        // 4) Scansiona le righe "action:" e popola la struttura 'actions'
    }

    public BufferedImage getSpriteImage() { return this.spriteImage; }
    public int getFrameWidth() { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
    public Rectangle getBoundingBox() { return boundingBox; }

    public List<FrameIndex> getFramesFor(String action, String direction) {
        // Restituisce la lista di FrameIndex (riga,colonna) per quell'azione/direzione
    }
}

Dove un FrameIndex è una piccola classe/struct che tiene traccia di riga e colonna di ogni frame aggiunto:

public class FrameIndex {
    public int row;
    public int col;
    // ...
}
```

Vantaggio: la definizione .sprite separa i dettagli di “quali frame appartengono all’azione X” dal codice Java, rendendo più facile estendere o modificare le animazioni.

### 1.4 La Classe `CharacterSpritesheet`

Nel nostro engine, **`CharacterSpritesheet`** è una specializzazione di **`Spritesheet`** che aggiunge:

1. **Supporto a più azioni e direzioni** (`Action` e `Direction`):  
   - Invece di una semplice mappa azione → lista di frame, usa un array tridimensionale: `images[actionIndex][directionIndex][frameIndex]`.  
   - Ogni `Action` (es: IDLE, WALK, ATTACK…) e ogni `Direction` (es: UP, DOWN, LEFT, RIGHT…) ha un suo vettore di frame.

2. **Gestione di bounding box direzionali**:  
   - Alcuni personaggi possono avere una bounding box leggermente diversa a seconda di come sono girati. `directionalBoundingBoxes` offre questa flessibilità.

3. **Parsing avanzato** del file `.sprite`:  
   - Può leggere righe come `directionalBoundingBox:LEFT:10-10-40-50` per definire bounding box specifiche in base alla direzione.  
   - Supporta anche la notazione `(f)` per indicare un frame che deve essere disegnato in modo “flippato” orizzontalmente (utile se vogliamo risparmiare sprite duplicati).

La struttura interna è la seguente:

```java
public class CharacterSpritesheet extends Spritesheet {
    private BufferedImage[][][] images;
    private Rectangle boundingBox;
    private Map<Direction, Rectangle> directionalBoundingBoxes;

    public CharacterSpritesheet(String fileName, int frameWidth, int frameHeight) {
        super(fileName, frameWidth, frameHeight, 0, 0);
        images = new BufferedImage[Action.values().length][Direction.values().length][];
        init();
    }

    // Oppure costruttore che carica i dati da un file .sprite
    public CharacterSpritesheet(String configFile) {
        loadSpriteData(configFile);
    }
    
    // ...
}
```

Gli attributi fondamentali sono:
- ```images```: un array 3D che memorizza i frame per (Action, Direction, frameIndex).
- ```boundingBox```: bounding box “generica” (usata se non è definita una bounding box specifica per direzione).
- ```directionalBoundingBoxes```: mappa da una direzione a una bounding box dedicata, utile per definire collisioni differenziate.

#### 1.4.1 Caricamento e Parsing (loadSpriteData)

Nel costruttore CharacterSpritesheet(String configFile), viene chiamato loadSpriteData(configFile), che:
1.	Apre il file .sprite come stream.
2.	Legge riga per riga.
3.	Se la riga inizia con ```fileName:```, memorizza il path all’immagine (.png).
4.	Se la riga inizia con ```frameWidth:```, frameHeight:, li salva come variabili.
5.	Se la riga inizia con ```boundingBox:```, popola un Rectangle boundingBox.
6.	Se la riga inizia con ```directionalBoundingBox:```, popola la mappa directionalBoundingBoxes.
7.	Se la riga inizia con ```action:```, legge l’Action, la Direction e l’elenco dei frame.

Esempio di riga action: ATTACK:RIGHT:0-0,0-1,0-2(f):
- ATTACK = tipo di azione.
- RIGHT = direzione.
- Frame: (riga=0, col=0), (riga=0, col=1), (riga=0, col=2, flipped). Il (f) indica che quel frame deve essere “ribaltato” orizzontalmente.

#### 1.4.2 Inizializzazione finale

Una volta lette tutte le righe, viene chiamato:

```java
initialize(spriteFile, frameWidth, frameHeight, 0, 0);
// ...
initializeFrames(frameData, flippedData);
this.boundingBox = boundingBox;
this.directionalBoundingBoxes = directionalBoundingBoxes;
```

- ```initialize(...)```: metodo ereditato da Spritesheet per caricare effettivamente l’immagine con ImageIO.read(...).
- ```initializeFrames(...)```: popola images[actionIndex][directionIndex][frameIndex] in base alle coordinate specificate, e gestisce il flip orizzontale se richiesto.

#### 1.4.3 Struttura dei Frame: images[action][direction][frame]

Nel metodo ```initializeFrames(...)```, abbiamo il seguente codice:

```java
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
                // Chiama getFlippedSprite(riga, col)
            } else {
                // Chiama getSprite(riga, col)
            }
        }
    }
}
```

- Iteriamo su tutti gli Action e tutte le Direction.
- frames è la lista di coordinate (riga, colonna) ricavate dal file .sprite.
- flippedFrames è una lista parallela di boolean, per sapere se quel frame va girato orizzontalmente.
- Le immagini finali vengono memorizzate nell'array images[action][direction][frame] per un rapido accesso a runtime.

#### 1.4.4 Bounding Box Generica

```java
public Rectangle getBoundingBox() {
    if (boundingBox == null) {
        // fallback di default
        return new Rectangle(0, 0, frameWidth, frameHeight);
    }
    return boundingBox;
}
```

Se nel file .sprite non è definito boundingBox:, la bounding box di default coincide con un rettangolo ampio come un frame.

#### 1.4.4 Bounding Box Direzionale

```java
public Map<Direction, Rectangle> getDirectionalBoundingBoxes() {
    if (directionalBoundingBoxes == null) {
        return new HashMap<>();
    }
    return directionalBoundingBoxes;
}
```

Se abbiamo linee del tipo:

```directionalBoundingBox:LEFT:10-10-40-50```

il sistema popola una Map<Direction, Rectangle> che, per la direzione LEFT, restituirà un box (10,10,40,50).
Questo permette di avere un box “asimmetrico” per l’animazione a sinistra vs. a destra.

5. Integrazione con SpriteComponent e AnimationSystem

All’atto pratico:
	1.	Creiamo un CharacterSpritesheet da un file .sprite (es: /player/Link.sprite).
	2.	In SpriteComponent, teniamo un riferimento a CharacterSpritesheet. Quando l’AnimationSystem decide quale frame disegnare (ad esempio, ATTACK, RIGHT, frameIndex=3), possiamo accedere a images[Action.ATTACK.getActionIndex()][Direction.RIGHT.getDirectionIndex()][3].
	3.	Bounding Box: a seconda della direzione corrente, possiamo usare getDirectionalBoundingBoxes().get(direction) o, se non esiste, usare la bounding box generica con getBoundingBox().
	4.	Draw: un eventuale RenderingSystem userà g.drawImage(...) sul frame. Se ci serve la bounding box per collisioni o debug, la useremo nelle verifiche di sovrapposizione.

### 1.4 Classe SpriteComponent.java

Questo componente rappresenta il “legame” tra un’entità e il suo sprite sheet (o animazioni). Di solito fornisce:
1.	Riferimento a un Spritesheet o CharacterSpritesheet.
2.	Timer e velocità di animazione (se l’animazione è gestita dal componente stesso o delegata all’AnimationSystem).
3.	Stato corrente (quale azione e direzione è selezionata in un determinato istante).
4.	Frame corrente da disegnare.

Esempio semplificato:

public class SpriteComponent extends Component {
    private Spritesheet spritesheet;
    private float animationSpeed;  // Esempio: 0.08f
    private boolean loop;
    private String currentAction = "IDLE";
    private String currentDirection = "DOWN";
    private List<FrameIndex> currentFrames;
    private int currentFrameIndex;
    private float elapsedTime;

    public SpriteComponent(Entity e, Spritesheet sheet, float animSpeed, boolean loop) {
        super(e);
        this.spritesheet = sheet;
        this.animationSpeed = animSpeed;
        this.loop = loop;
        // Carichiamo i frame dell'azione/direzione iniziale
        this.currentFrames = spritesheet.getFramesFor(currentAction, currentDirection);
    }

    // Metodi per cambiare azione/direzione, resettare l’animazione, ecc.
    // ...
}

Questo SpriteComponent verrà aggiornato dall’AnimationSystem per cambiare il frame corrente in base al tempo trascorso, e letto dal RenderingSystem per disegnare il frame corretto.

4. Classe AnimationSystem.java

Si occupa di far “scorrere” i frame nel SpriteComponent. Lo pseudocodice potrebbe essere:

public class AnimationSystem extends BaseSystem {
    @Override
    public void update(Engine engine, double dt) {
        // 1. Per ogni entità che ha un SpriteComponent:
        //    - incrementare sprite.elapsedTime di dt
        //    - se sprite.elapsedTime >= sprite.animationSpeed:
        //        + passare al frame successivo
        //        + sprite.elapsedTime = 0
        //    - gestire fine animazione (se non loop, rimanere sull’ultimo frame ecc.)
    }

    @Override
    public void render(Engine engine, Graphics2D g) {
        // Non fa nulla, l'animazione è "logica", non disegniamo qui
    }
}

Alcuni progetti usano un componente dedicato all’animazione (tipo AnimationComponent) e lasciano a SpriteComponent solo la parte di disegno. Altri (come questo) possono unire i concetti in un unico componente. L’importante è che il sistema abbia tutte le info necessarie per aggiornare i frame in base al tempo.

5. Dichiarazione del Player nello Stato (Metodo init)

Ecco un esempio (come da richiesta) di come creare l’entità player e i suoi componenti. Nel tuo State (o Scene), hai un metodo init dove vengono create entità e sistemi:

// 1. Creiamo l'entità
Entity player = new Entity(EntityType.PLAYER, 2);

// 2. Carichiamo lo spritesheet
CharacterSpritesheet playerSpritesheet = new CharacterSpritesheet("/player/Link.sprite");

// 3. Aggiungiamo un MotionComponent (posizione e velocità)
MotionComponent playerPosition = new MotionComponent(player, 100, 100, 300.0f);
player.addComponent(playerPosition);

// 4. Aggiungiamo un componente di input da tastiera
KeyInputComponent playerInput = new KeyInputComponent(player);
player.addComponent(playerInput);

// 5. Aggiungiamo il componente Sprite (collega l'entità al file .sprite)
SpriteComponent playerSprites = new SpriteComponent(player, playerSpritesheet, 0.08f, true);
player.addComponent(playerSprites);

// 6. Registriamo l'entità nel motore/scene
add(player);

Spiegazione step-by-step:
	1.	Entity: EntityType.PLAYER può essere un enum o costante per distinguere tipologie (giocatore, nemico, oggetto…). Il valore 2 potrebbe rappresentare un livello di “z-order” o priorità di rendering.
	2.	Spritesheet: "/player/Link.sprite" è il percorso del file .sprite. La classe CharacterSpritesheet (estensione di Spritesheet) leggerà e parserà il file, caricando l’immagine e definendo le azioni.
	3.	MotionComponent: definisce la posizione (100,100) e una velocità massima o costante 300.0f (a seconda di come lo gestisci).
	4.	KeyInputComponent: collega i tasti premuti dal giocatore a certi movimenti/azioni.
	5.	SpriteComponent: unisce l’entità allo Spritesheet, con velocità di animazione 0.08f. Se true, l’animazione fa loop, altrimenti si ferma all’ultimo frame.
	6.	add(player): aggiunge l’entità al mondo di gioco (o all’engine).

6. Definizione dei Sistemi

Alla fine del metodo init, aggiungiamo i sistemi di input, motion, animazione, rendering:

// Systems:
InputSystem inputSystem = new InputSystem(this.engine);
inputSystem.bindAction(InputAction.MOVE_LEFT, KeyEvent.VK_A);
inputSystem.bindAction(InputAction.MOVE_RIGHT, KeyEvent.VK_D);
inputSystem.bindAction(InputAction.MOVE_UP, KeyEvent.VK_W);
inputSystem.bindAction(InputAction.MOVE_DOWN, KeyEvent.VK_S);

// Esempio di azione personalizzata: PAUSE
inputSystem.bindCustomAction(InputAction.PAUSE, KeyEvent.VK_P, deltaTime -> {
    EngineState currentState = engine.getStateManager().getCurrentState();
    if (currentState == EngineState.RUNNING) {
        engine.getStateManager().requestStateChange(EngineState.PAUSED);
    } else if (currentState == EngineState.PAUSED) {
        engine.getStateManager().requestStateChange(EngineState.RUNNING);
    }
});
inputSystem.addDebouncedAction(InputAction.PAUSE);

// Esempio di azione personalizzata: DEBUG
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

// Aggiunta degli altri sistemi
add(new MotionSystem(this.engine));
add(new AnimationSystem(this.engine));
add(new RenderingSystem(this.engine));

	1.	InputSystem: definisce le mappature tasto -> azione (MOVE_LEFT => VK_A).
	2.	MotionSystem: sposta il player in base a velocità/accelerazione.
	3.	AnimationSystem: aggiorna i frame di animazione.
	4.	RenderingSystem: disegna entità (compreso il player).

7. Flusso di Esecuzione

Durante il game loop:
	1.	InputSystem: legge se stai premendo “A” (→ move left), quindi modifica i componenti di movimento (MotionComponent) o SpriteComponent (magari cambiando currentDirection = "LEFT").
	2.	MotionSystem: aggiorna la posizione (x,y) in base alla velocità. Se stai premendo A, velocity.x sarà negativa.
	3.	AnimationSystem: controlla se l’azione è “WALK” o “IDLE” e avanza il frame in base a dt.
	4.	RenderingSystem: legge la posizione corrente e il frame corrente dal SpriteComponent e disegna l’immagine corretta sullo schermo.

8. Riepilogo

	•	Lo sprite sheet definito da un file .sprite ci dà una configurazione esterna e facile da modificare.
	•	Il Spritesheet (classe Java) carica i dati dal file, li memorizza e li rende disponibili ai sistemi.
	•	Il SpriteComponent collega un’entità con uno sprite sheet o un’animazione (frame corrente, velocità di cambio frame, ecc.).
	•	L’AnimationSystem si occupa di progredire nei frame in base al tempo.
	•	L’InputSystem, il MotionSystem e il RenderingSystem completano il ciclo di vita del nostro personaggio animato.

In questo modo, la gestione dell’animazione è modulare: se vogliamo cambiare i frame di WALK o aggiungere un’azione ATTACK, basta aggiornare il file .sprite. Se vogliamo modificare la velocità di animazione, interveniamo su SpriteComponent (o su un eventuale AnimationComponent). Questa flessibilità è uno dei punti di forza dell’approccio ECS combinato con l’uso di file di configurazione esterni.
### 2.2 Estrazione di frame
In genere, per estrarre un frame da uno sprite sheet, utilizziamo un rettangolo di coordinate `(x, y, width, height)`. Se i frame sono equidistanti e della stessa dimensione, basta:
```java
int frameX = currentFrameIndex * frameWidth;
int frameY = animationRowIndex * frameHeight;
BufferedImage frame = spriteSheet.getSubimage(frameX, frameY, frameWidth, frameHeight);
```

Il sistema di animazione si occupa di aggiornare currentFrameIndex a seconda del tempo trascorso.

## 3. Movimento con Eulero semi-implicito

Il MotionSystem aggiornando posizione e velocità delle entità basate su un modello semplificato di fisica utilizza spesso il metodo di Eulero semi-implicito (o “Symplectic Euler”). In pseudo-codice:

velocity = velocity + (acceleration * deltaTime)
position = position + (velocity * deltaTime)

La differenza dal metodo di Eulero “classico” è che aggiorniamo prima la velocità e poi la posizione (usando la nuova velocità). Ciò riduce alcuni problemi di stabilità numerica.

## 4. Panoramica dei quattro sistemi principali

### 4.1 InputSystem.java

Gestisce gli input del giocatore. Ad esempio:

```java
public class InputSystem extends BaseSystem {
    @Override
    public void update(Engine engine, double dt) {
        // 1. Leggere i tasti premuti (tramite un InputHandler o simile).
        // 2. Trovare l'entità "giocatore" (magari con un PlayerTagComponent).
        // 3. Aggiornare un componente di movimento (es. impostando un’accelerazione
        //    in base ai tasti freccia).
    }

    @Override
    public void render(Engine engine, Graphics2D g) {
        // Di solito qui non disegniamo nulla. L'InputSystem è logico.
    }
}
```

- Può interpretare la pressione dei tasti (sinistra, destra, salto…) e modificare un VelocityComponent o AccelerationComponent.
- Se l’entità giocatore possiede un PlayerInputComponent, possiamo distinguere più facilmente chi deve muoversi.

### 4.2 MotionSystem.java

Si occupa della fisica di base e del calcolo della nuova posizione:

```java
public class MotionSystem extends BaseSystem {
    @Override
    public void update(Engine engine, double dt) {
        // 1. Trova entità con i componenti di movimento (Transform, Velocity, ecc.).
        // 2. Per ciascuna, aggiorna velocity e position con Eulero semi-implicito.
    }

    @Override
    public void render(Engine engine, Graphics2D g) {
        // Non disegna nulla, il rendering è compito di un altro sistema.
    }
}
```

- Usando VelocityComponent e TransformComponent, applichiamo:

velocity.x += acceleration.x * dt;
velocity.y += acceleration.y * dt;
position.x += velocity.x * dt;
position.y += velocity.y * dt;


- Si possono aggiungere limiti di velocità, gravità, attriti, collisioni semplificate, ecc.

### 4.3 AnimationSystem.java

Gestisce la logica di animazione:

```java
public class AnimationSystem extends BaseSystem {
    @Override
    public void update(Engine engine, double dt) {
        // 1. Trova le entità con un AnimationComponent (che contiene info su frame, timer, ecc.).
        // 2. Avanza il timer di animazione, passa al frame successivo se necessario.
        // 3. Reset o cambio animazione se si passa da idle a running, etc.
    }

    @Override
    public void render(Engine engine, Graphics2D g) {
        // Non disegna, si limita a impostare il frame corrente nel componente di animazione.
    }
}
```

	•	L’AnimationComponent potrebbe avere:
	•	currentFrameIndex
	•	timePerFrame (quanto tempo dura un frame)
	•	elapsedTime (quanto tempo è trascorso nel frame corrente)
	•	loop o once (se l’animazione deve ripetersi o no)
	•	A ogni update, se elapsedTime >= timePerFrame, incrementiamo currentFrameIndex e resettiamo elapsedTime.
	•	Possiamo anche gestire stati diversi: ad esempio, “idle”, “walk”, “jump”, ognuno con la sua sequenza di frame.

### 4.4 RenderingSystem.java

L’ultimo anello della catena, si occupa di disegnare l’entità sullo schermo, usando i dati forniti dai componenti di transform e animation:

```java
public class RenderingSystem extends BaseSystem {
    @Override
    public void update(Engine engine, double dt) {
        // Di solito non facciamo nulla qui, a meno che vogliamo un "pre-render" step.
    }

    @Override
    public void render(Engine engine, Graphics2D g) {
        // 1. Trova entità con un componente "SpriteComponent" o "AnimationComponent".
        // 2. Recupera la posizione (Transform) e il frame attuale dell'animazione.
        // 3. Disegna l’immagine (frame) nella posizione (x, y).
    }
}
```

	•	Tipicamente, cerchiamo un SpriteComponent (che punta a un’immagine singola) o un AnimationComponent (dove il frame corrente è già calcolato da AnimationSystem).
	•	Utilizziamo g.drawImage(...) per disegnare il frame alla posizione desiderata.

## 5. Movimenti realistici: Esempio di Eulero semi-implicito

### 5.1 Caso semplice

Supponiamo un personaggio che si muove a destra spinto dalla pressione del tasto →:
1.	InputSystem: se il tasto → è premuto, acceleration.x = 10 (unità a seconda del nostro sistema).
2.	MotionSystem:
velocity.x += 10 * dt;
position.x += velocity.x * dt;
3.	AnimationSystem: se velocity.x > 0.1, passiamo all’animazione di “walk” a destra.
4.	RenderingSystem: disegna il frame corrente di “walk” alla position.x calcolata.

## 6. Rendering delle entity

Nel loop di rendering (GamePanel), dopo l’update, chiamiamo:

engine.render(g2D);

In engine.render(...), tutti i sistemi eseguono la loro render(...). Nel caso del RenderingSystem, esso:
	1.	Scorre le entità.
	2.	Verifica se c’è un SpriteComponent o AnimationComponent.
	3.	Disegna il frame corrente alla giusta posizione.

Se abbiamo uno scaling dinamico (come visto in lezioni precedenti), il GamePanel potrebbe già aver applicato una trasformazione scale(...) per adattare il disegno alla finestra.
