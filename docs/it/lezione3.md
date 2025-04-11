# Lezione 3 – Sprite, Movimento e Animazione

## Obiettivi
- Comprendere come caricare e gestire **sprite** per creare l’animazione del personaggio giocante.  
- Approfondire i **sistemi** di movimento, input, animazione e rendering.  
- Introdurre il **metodo di Eulero semi-implicito** per aggiornare posizione e velocità in modo “realistico”.  
- Mostrare come avviene il **rendering** delle entità animabili.

---

## Introduzione
Finora abbiamo costruito l’**infrastruttura** del gioco con il pattern ECS: entità, componenti, sistemi. Per fare muovere il player sul campo di gioco, abbiamo bisogno dei seguenti elementi:
- **Input**: gestire i comandi impartiti dall'utente.  
- **Animazione**: gestire una sequenza di immagini (frame) che si susseguono per dare l’illusione del movimento.  
- **Movimento** “realistico”: aggiornare posizione e velocità di un’entità in base a forze, accelerazioni e input del giocatore.  
- **Rendering**: disegnare il frame corrente dell’animazione sullo schermo, nella posizione corretta.

I quattro sistemi principali che svolgono le funzioni necessarie sono:
1. **`InputSystem`** e **`KeyInputComponent`**: interpreta i comandi dell’utente (tastiera, mouse, gamepad…).  
2. **`MotionSystem`** e **`MotionComponent`**: calcola e applica gli aggiornamenti di posizione usando Eulero semi-implicito.  
3. **`AnimationSystem`** e **`SpriteComponent`**: gestisce i cicli di animazione, passando da un frame all’altro.  
4. **`RenderingSystem`**: disegna effettivamente le entità sullo schermo con le coordinate e i frame adeguati.

---

## 1 Gestione dell'input

Nel progetto, l’input da tastiera è gestito in modo centralizzato tramite l’ActionStateManager, una classe “globale” che conserva lo stato di tutte le azioni di gioco ("MOVE_LEFT", "MOVE_RIGHT", ecc.). Le entità che hanno bisogno di reagire ai comandi da tastiera possiedono un KeyInputComponent, il quale, invece di memorizzare localmente gli stati dei tasti o delle azioni, si limita a chiederli direttamente all’ActionStateManager. Il sistema che si occupa di convertire i tasti fisici in azioni di gioco è l’InputSystem. A ogni frame, l’InputSystem legge i tasti premuti e rilasciati dall’utente (forniti dalla classe KeyboardInputHandler, Listener di JFrame) e aggiorna l’ActionStateManager.

### 1.1	KeyInputComponent

Il KeyInputComponent è un componente che le entità (in genere il player) possono avere per “accedere” agli input. La sua implementazione è minimale, perché non memorizza gli stati internamente. La struttura è la seguente:

```java
public class KeyInputComponent extends Component {
private boolean enabled;

public KeyInputComponent(Entity entity) {
    super(entity);
    enabled = true;
}

public boolean isEnabled() {
    return enabled;
}

public void setEnabled(boolean enabled) {
    this.enabled = enabled;
}

public boolean isActionActive(String action) {
    boolean isActive = ActionStateManager.isActionActive(action);
    return enabled && isActive;
}

}
```

- La proprietà “enabled” permette di abilitare o disabilitare il component, per esempio per bloccare l’input in certe situazioni.
- Il metodo isActionActive(String action) si limita a chiamare ActionStateManager.isActionActive(action). Se l’entità non è abilitata (enabled = false), restituisce false anche se l’azione è attiva.

Di conseguenza, quando un sistema (come per esempio il MotionSystem) vuole sapere se il giocatore sta premendo il tasto corrispondente all'azione “MOVE_LEFT”, recupera il KeyInputComponent del player e invoca il metodo isActionActive(“MOVE_LEFT”). Non deve manipolare direttamente tasti o KeyEvent poiché se ne occupano l’ActionStateManager e l’InputSystem.

### 1.2	ActionStateManager

Questa classe statica si comporta come un registro centralizzato per gli stati di tutte le azioni registrate. Contiene:
- Una mappa actionStates (String -> Boolean), che serve a memorizzare lo stato delle azioni, cioè se in un preciso istante un’azione è attiva o meno.
- Un set consumedActions, per gestire azioni già elaborate (ad esempio, un’azione one-shot che vogliamo disattivare subito dopo l’utilizzo).

I metodi principali sono:
- `activateAction(String action)`: imposta a true l’azione.
- `deactivateAction(String action)`: imposta a false l’azione.
- `consumeAction(String action)`: se l’azione era attiva, la sposta in consumedActions e la forza immediatamente a false, in modo che non risulti più attiva fino a quando il tasto non viene rilasciato.
- `isActionActive(String action)`: verifica se la mappa la considera attiva e non sia già stata elaborata.
- `resetConsumedActions()`: ripulisce il set consumedActions.

Pertanto, l’ActionStateManager gestisce le azioni di gioco. Qualsiasi parte del codice (KeyInputComponent, sistemi di combattimento, menù, ecc.) può chiamare isActionActive per verificare lo stato di un’azione.

### 1.3		InputSystem

L’InputSystem è un sistema ECS che ha la responsabilità di:
- Leggere i tasti premuti e rilasciati dal KeyboardInputHandler (che fa da KeyListener e memorizza l’elenco di pressedKeys e releasedKeys a ogni frame).
- Applicare un meccanismo di binding (action -> keyCode) per associare, per esempio, l'azione di muoversi a sinistra (“MOVE_LEFT”) alla pressione del tasto 'a' (KeyEvent.VK_A).
- Distinguere tra azioni “normali” e “debounced” (cioè che devono attivarsi una sola volta per ogni pressione del tasto).
- Aggiornare l’ActionStateManager di conseguenza.
- Eseguire eventuali custom actions (codice personalizzato da eseguire immediatamente quando si rileva che un’azione è attiva).

Ecco una descrizione del flusso nel metodo update(float deltaTime):
1.	ActionStateManager.resetConsumedActions(): all’inizio di ogni frame si pulisce l’elenco delle azioni consumate.
2.	L’InputSystem recupera `pressedKeys` e `releasedKeys` dal KeyboardInputHandler. `pressedKeys` è l’insieme di tasti attualmente tenuti premuti, mentre `releasedKeys` contiene i tasti che sono stati rilasciati.
3.	buildActionStates(pressedKeys, releasedKeys) crea una mappa temporanea che indica, per ogni azione registrata nei binding, se è true (attiva) o false (inattiva). Se un’azione è “debounced”, l’InputSystem la attiva solo la prima volta che il tasto risulta premuto, e la disattiva immediatamente dopo, finché non viene rilasciato e premuto nuovamente.
4.	Per ogni entry di tale mappa (action -> true/false), l’InputSystem sincronizza lo stato globale delle azioni chiamando:
    - `ActionStateManager.activateAction(action)` se è true
    - `ActionStateManager.deactivateAction(action)` se è false
5.	`executeCustomKeyActions(deltaTime)`: controlla le azioni per cui esiste un callback personalizzato. Se isActionActive(action) è true, viene chiamato il codice associato. Se l'azione è “one-shot”, il codice viene eseguito immediatamente e l'azione viene disattivata.
6.	Viene ripulito `releasedKeys` dal KeyboardInputHandler, così che non vengano riutilizzati il frame successivo.


### 1.4	Esempio di Flusso Completo
- L’utente preme il tasto 'a', corrispondente all'azione “MOVE_LEFT”. Il KeyboardInputHandler aggiunge quel keyCode ai pressedKeys.
- Durante l’update di InputSystem, pressedKeys contiene keyCode = 65 (corrispondente ad “A”), e la mappa dei binding dice che “MOVE_LEFT” è associato a 65. 
- L’InputSystem chiama ActionStateManager.activateAction(“MOVE_LEFT”) e lo stato dell'azione “MOVE_LEFT” diventa true.
- Se quell’azione è debounced, l’InputSystem assicura che venga attivata solo una volta e non resti in loop continuo.
- Il MotionSystem, quando per esempio deve aggiornare l'entity del giocatore, recupera il KeyInputComponent e chiama il metodo `KeyInputComponent.isActionActive(“MOVE_LEFT”)`. A seconda dello stato (true o false), regola il movimento di conseguenza.
- Nell'agiornamento successivo, se il giocatore continua a tenere premuto il tasto, l’InputSystem vedrà di nuovo pressedKeys con lo stesso codice. Se non è debounced, rimarrà attivo, altrimenti verrà disabilitato.
- Infine, se l’utente rilascia il tasto, appare in releasedKeys, e l’InputSystem lo imposta l'azione a false con ActionStateManager.deactivateAction(“MOVE_LEFT”).

### 1.5	Configurazione dell'InputSystem

Per impostare correttamente l'InputSystem, dopo aver creato l'istanza dell’InputSystem, bisogna configurare le associazioni azione->tasto nel metodo init() dello stato di gioco. La classe InputAction contiene le costanti che rappresentano le azioni MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN. A ciascuna può essere associato un tasto specifico. Per esempio, nel caso dei tasti WASD:

```java
InputSystem inputSystem = new InputSystem(this.engine);
inputSystem.bindAction(InputAction.MOVE_LEFT, KeyEvent.VK_A);
inputSystem.bindAction(InputAction.MOVE_RIGHT, KeyEvent.VK_D);
inputSystem.bindAction(InputAction.MOVE_UP, KeyEvent.VK_W);
inputSystem.bindAction(InputAction.MOVE_DOWN, KeyEvent.VK_S);
```

Ci sono azioni che eseguono codice specifico quando attive, per esempio mettere in pausa il gioco o attivare la modalità debug. Per la pausa (con tasto “P”), si deve creare un binding che, invece di impostare semplicemente lo stato dell’azione, chiama una callback:

```java
inputSystem.bindCustomAction(“PAUSE”, KeyEvent.VK_P, deltaTime -> {
    EngineState currentState = engine.getStateManager().getCurrentState();
    if (currentState == EngineState.RUNNING) {
        engine.getStateManager().requestStateChange(EngineState.PAUSED);
    } else if (currentState == EngineState.PAUSED) {
        engine.getStateManager().requestStateChange(EngineState.RUNNING);
    }
});
```

Se l’azione “PAUSE” deve essere “debounced” (cioè eseguita una sola volta per pressione), dev'essere aggiunta alle debouncedActions:

```java
inputSystem.addDebouncedAction(“PAUSE”);
```

L’azione di debug, invece, può essere mappata sul tasto 0 della tastiera:

```java
inputSystem.bindCustomAction(“DEBUG”, KeyEvent.VK_0, deltaTime -> {
    if (engine.isDebug()) {
        engine.setDebug(false);
    } else {
        GamePanel.resetDebug();
        engine.setDebug(true);
    }
});
inputSystem.addDebouncedAction(“DEBUG”);
```

Questa callback controlla se il motore è in modalità debug. Se sì la disabilita, altrimenti la abilita. Anche questa azione dev'essere “debounced”, altrimenti la modalità di debug continuerebbe ad attivarsi e disattivarsi mentre il tasto rimane premuto.

---
## 2 Gestione del movimento

Il MotionSystem si occupa di aggiornare la posizione e talvolta la velocità delle entità che possiedono un MotionComponent. In base alla presenza di altre componenti (KeyInputComponent, NPCComponent, AttackComponent, ecc.), il movimento può essere gestito diversamente (player, NPC, entità generiche).

Il MotionComponent, invece, conserva le informazioni fondamentali su posizione, velocità e parametri di accelerazione/decelerazione di un’entità.

### 2.1	MotionComponent

La classe MotionComponent estende Component e contiene vari campi numerici per la gestione del movimento:
- x, y: posizione corrente dell’entità.
- oldX, oldY: posizione precedente, utile per calcolare spostamenti o fare rollback se necessario.
- vx, vy: velocità in orizzontale e verticale.
- maxSpeed: velocità massima consentita.
- acceleration, deceleration: parametri usati per aumentare o diminuire gradualmente la velocità.

Struttura del costruttore:
```java
public MotionComponent(Entity entity, float x, float y, float maxSpeed) {
    super(entity);
    this.x = this.oldX = x;
    this.y = this.oldY = y;
    this.maxSpeed = maxSpeed;
    this.vx = this.vy = 0;
    this.acceleration = 1000f;
    this.deceleration = 1000f;
}
```

Il metodo move(float deltaX, float deltaY) aggiorna la posizione x,y e salva i valori precedenti in oldX, oldY. Durante l’update, si può chiamare per esempio `move(vx * deltaTime, vy * deltaTime)`.

### 2.2	MotionSystem

Il MotionSystem estende BaseSystem e ha priorità 2, ciò significa che è tra i primi a essere aggiornato (dopo gli input, che hanno priorità 1). Nel metodo initStateUpdateMap, si decide in quali stati del gioco aggiornare la posizione (RUNNING, CUTSCENE, ENTERING…). Il cuore del sistema è il metodo `updateEntity(Entity entity, float deltaTime)`, che ha la seguente struttura:

```java
protected void updateEntity(Entity entity, float deltaTime) {
MotionComponent mc = entity.getComponent(MotionComponent.class);
if (mc == null) return;  // Se l’entità non ha MotionComponent, non facciamo nulla.

// 1) Verifica attacchi o lancio di proiettile:
AttackComponent ac = entity.getComponent(AttackComponent.class);
ThrowProjectileComponent tpc = entity.getComponent(ThrowProjectileComponent.class);
boolean isPerformingAction = (ac != null && ac.isAttacking()) ||
                             (tpc != null && tpc.isThrowing());
if (isPerformingAction) {
    // Se l’entità sta attaccando o lanciando, blocchiamo il movimento
    mc.setVx(0);
    mc.setVy(0);
    // Aggiorna la posizione con spostamento zero
    mc.move(0, 0);
    return;
}

// 2) In base alle componenti specifiche:
if (entity.hasComponent(KeyInputComponent.class)) {
    // Entità gestita come “player”
    handlePlayerMovement(entity, deltaTime, mc);
    return;
}

if (entity.hasComponent(NPCComponent.class)) {
    // Entità gestita come “NPC”
    handleNPCMovement(entity, deltaTime);
    return;
}

// 3) Altrimenti gestione “generica”
handleGenericMovement(entity, deltaTime, mc);

}
```

In sintesi:
- Se l’entità sta eseguendo un attacco o un lancio di proiettile, imponiamo vx=vy=0 (nessun movimento).
- Se ha il componente KeyInputComponent, la tratteremo come player.
- Se ha il componente NPCComponent, la tratteremo come NPC.
- Altrimenti, applichiamo un semplice aggiornamento generico.

Il metodo `handlePlayerMovement(…)` è specifico per gestire il movimento del giocatore. Legge i comandi da KeyInputComponent (moveLeft, moveRight, ecc.) e aggiorna velocità e posizione usando il metodo di Eulero semi-implicito (anche chiamato “symplectic Euler”), una variante del classico metodo di Eulero per risolvere numericamente le equazioni del moto, con il vantaggio di essere più stabile in alcune situazioni (specialmente nei sistemi fisici semplificati).

Il metodo di Eulero “classico” aggiornerebbe posizione e velocità in questo ordine:
1.	position = position + velocity * deltaTime
2.	velocity = velocity + acceleration * deltaTime

Il metodo semi-implicito (o “symplectic”) inverte l’ordine:
1.	velocity = velocity + acceleration * deltaTime
2.	position = position + velocity * deltaTime

La differenza chiave è che, nel metodo semi-implicito, quando calcoliamo la nuova posizione usiamo già la velocità aggiornata. Questo riduce alcuni problemi di instabilità che possono emergere con Eulero esplicito, soprattutto se si hanno sistemi con accelerazioni variabili.

Nel contesto di un gioco 2D:
- “acceleration” può dipendere dalla gravità, dai tasti premuti, dall’attrito, ecc.
- “velocity” e “position” sono i valori contenuti nel MotionComponent.

Alla fine di ogni frame, si calcola:
1.	`velocity.x += acceleration.x * deltaTime` e `velocity.y += acceleration.y * deltaTime`
2.	`position.x += velocity.x * deltaTime` e `position.y += velocity.y * deltaTime`

In molti giochi 2D, questa formula è sufficiente per generare movimenti fluidi e relativamente stabili. Le azioni che il MotionSystem compie sono le seguenti:

- Verifica quali tasti per il movimento orizzontale sono attivi (es. `keyInput.isActionActive(InputAction.MOVE_LEFT)`).
- Se per esempio moveLeft è true (e moveRight no), `vx -= acceleration * deltaTime`. Viceversa per moveRight.
- Se nessun tasto è premuto, viene applicata la decelerazione.
- Se è presente una GravityComponent (scenario platform), la velocità verticale è gestita dall'azione salto (InputAction.JUMP) e dalla gravità.
- Nel caso di uno scenario top-down, moveUp e moveDown vengono gestite analogamente a moveLeft e moveRight. 
- Se la velocità risultante supera maxSpeed, viene limitata per evitare che continui ad aumentare all'infinito.
- Infine, viene aggiornato il MotionComponent: `mc.setVx(vx)` e `mc.setVy(vy)` per cambiare la velocita, `mc.move(vx * deltaTime, vy * deltaTime)` per modificare la posizione.

---

## 3. Sprite Sheet e Animazioni

Con [sprite](https://it.wikipedia.org/wiki/Sprite_(informatica)), in informatica, si indica una bitmap, un’immagine raster composta da pixel e generalmente 2D, che fa parte di una scena più grande (lo "sfondo") e che può essere spostata in maniera indipendente rispetto ad essa. Può essere sia statica, sia dinamica. Gli sprite furono inventati per gestire in maniera rapida ed efficiente speciali porzioni di grafica nei videogiochi usando hardware appositamente dedicato, poiché allora le CPU non erano sufficientemente potenti per compiere tali operazioni. Il Texas Instruments TMS9918 è il primo chip video nella cui documentazione viene per la prima volta utilizzato ufficialmente il termine "sprite" per indicare questi oggetti grafici. Secondo Karl Guttag, uno dei due ingegneri che sviluppò il TMS9918, il termine deriva dal fatto che i dati degli sprites, piuttosto che far parte della stessa area di memoria video dello sfondo, ci fluttuano sopra da un’altra area di memoria senza alterarne il contenuto, come un fantasma o uno spirito mitologico.

 Uno sprite sheet è un’unica immagine che contiene **più frame** di animazione. Ad esempio, potremmo avere un file PNG con tutte le pose del personaggio: idle, camminata, salto, ecc., disposte in griglia. Per disegnare il frame corrente, estraiamo (o “clippiamo”) la porzione di immagine corrispondente alla posa desiderata e la stampiamo sullo schermo. Per creare uno spritesheet, si può usare l’applicazione open source [Universal LPC Spritesheet Generator](https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/). Si possono trovare anche risorse on line, per esempio ai seguenti siti:
- [Open Art](https://opengameart.org/)
- [CraftPix](https://craftpix.net/categorys/2d-game-kits/)

### 3.1 Definizione dello Sprite Sheet

Un **sprite sheet** è un’unica risorsa grafica (un file `.png` o `.jpg`) che contiene più **frame** di animazione. Nel nostro progetto, diamo anche la possibilità di definire un file `.sprite` che descrive:
1. **Il nome del file immagine** (es. `fileName:/spritesheet/player.png`).  
2. **Le dimensioni** di ogni frame (`frameWidth`, `frameHeight`).  
3. **Bounding box** di default, usata per collisioni o calcolo di offset (`boundingBox:58-58-28-28`).  
4. **Azioni** (`action`) con la lista dei frame:  
   - Esempio: `action:IDLE:DOWN:8-0` significa che per l’azione `IDLE`, direzione `DOWN`, usiamo i frame sulla **riga 8** e **colonna 0**.  
   - Se abbiamo una sequenza, la sintassi `action:ATTACK:RIGHT:0-0,0-1,0-2,...` elenca i vari frame da disegnare in quell’ordine.

### 3.2 Esempio di file `.sprite`
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

### 3.3. La classe `Spritesheet.java`
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

### 3.4 La Classe `CharacterSpritesheet`

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

#### 3.4.1 Caricamento e Parsing (loadSpriteData)

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

#### 3.4.2 Inizializzazione finale

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

#### 3.4.3 Struttura dei Frame: images[action][direction][frame]

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

#### 3.4.4 Bounding Box Generica

```java
public Rectangle getBoundingBox() {
    if (boundingBox == null) {
        // fallback di default
        return new Rectangle(0, 0, frameWidth, frameHeight);
    }
    return boundingBox;
}
```

Se nel file .sprite non è definito ```boundingBox:```, la bounding box di default coincide con un rettangolo ampio come un frame.

#### 3.4.5 Bounding Box Direzionale

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

il sistema popola una ```Map<Direction, Rectangle>``` che, per la direzione LEFT, restituirà un box (10,10,40,50).
Questo permette di avere una bounding box diversa a seconda della direzione.

### 3.5 Classe SpriteComponent.java

Questo componente rappresenta il “legame” tra un’entità e il suo sprite sheet (o animazioni). 

Esempio semplificato:

```java
public class SpriteComponent extends Component {
    private BufferedImage[][][] images; 
    private int currentFrame;
    private float frameDuration;
    private float elapsedTime;
    private int frameWidth;
    private int frameHeight;
    private Direction direction;
    private Action action;
    protected boolean looping; //  Flag indicating whether the animation should loop or play once         
    private Runnable onAnimationEnd;

    public SpriteComponent(Entity entity, CharacterSpritesheet spritesheet, float frameDuration, boolean looping) {
    	super(entity);
    	if (spritesheet == null) {
            throw new IllegalArgumentException("Spritesheet cannot be null.");
        }
        this.frameDuration = frameDuration;
        this.looping = looping;
        this.onAnimationEnd = null;
        this.currentFrame = 0;
        this.elapsedTime = 0.0f;
        this.direction = Direction.RIGHT; // Default direction
        this.action = Action.IDLE; // Default action
        this.images = spritesheet.getImages();
        this.frameWidth = spritesheet.getFrameWidth();
        this.frameHeight = spritesheet.getFrameHeight();
    }

    // Metodi per cambiare azione/direzione, resettare l’animazione, ecc.
    // ...
}
```

Questo SpriteComponent verrà aggiornato dall’AnimationSystem per cambiare il frame corrente in base al tempo trascorso, e letto dal RenderingSystem per disegnare il frame corretto.
Le funzionalità principali sono:

1. Prendere il CharacterSpritesheet come parametro del costruttore:
   - Questo ci fornisce l’array ```images[action][direction][frame]``` e le bounding box, se necessario.
2. Gestire l’azione e la direzione correnti:
   - currentAction (es: ```IDLE```, ```WALK```, ```ATTACK```).
   - currentDirection (es: ```UP```, ```DOWN```, ```LEFT```, ```RIGHT```).
3. Tempi di animazione e frame:
   - ```frameDuration``` definisce quanto durano i frame (es: se frameDuration = 0.08f, ogni 0.08 secondi passiamo al frame successivo).
   - ```elapsedTime``` accumula il tempo trascorso dall’ultimo cambio di frame.
   - ```currentFrame``` indica quale frame stiamo usando.
4. Looping:
   - Se ```true```, quando arriviamo all’ultimo frame, torniamo all'indice 0.
   - Se ```false```, restiamo sull’ultimo frame (animazione “one-shot”) e, se definito, mandiamo in esecuzione il codice custom ```onAnimationEnd```.


### 3.6 Classe AnimationSystem.java
La classe **`AnimationSystem`** è un sistema che si occupa di **aggiornare le animazioni** di entità che possiedono un `SpriteComponent` e, se necessario, altre componenti correlate (`MotionComponent`, `AttackComponent`, ecc.). Inoltre, gestisce le animazioni di tile map (se esistono `TileMapComponent`). Di seguito l'analisi del codice.

#### 3.6.1 Dichiarazione e Costruttore

```java
public class AnimationSystem extends BaseSystem {
    public AnimationSystem(Engine engine) {
        super(engine, 9);
    }
    
    ...
}
```

- Estende BaseSystem: fornisce metodi come updateEntity per gestire la logica sulle entità.
- Il parametro 9 nel costruttore indica la priorità con cui questo sistema viene aggiornato rispetto agli altri (in questo engine, più alto è il numero, più tardi viene chiamato).

#### 3.6.2 initStateUpdateMap()

```java
@Override
protected void initStateUpdateMap() {
    setUpdateInState(EngineState.STARTING, false);
    setUpdateInState(EngineState.RUNNING, true);
    setUpdateInState(EngineState.CUTSCENE, true);
    setUpdateInState(EngineState.PAUSED, false);
    setUpdateInState(EngineState.SHOWING_DIALOG, false);
    setUpdateInState(EngineState.EXITING, false);
    setUpdateInState(EngineState.ENTERING, true);
}
```

Questo metodo configura in quali stati del motore il sistema deve essere aggiornato:
- ```RUNNING```, ```CUTSCENE``` e ```ENTERING```: true (il sistema esegue l’update).
- ```STARTING```, ```PAUSED```, ```SHOWING_DIALOG```, ```EXITING```: false (non viene aggiornato).
- È un modo per sospendere o abilitare il sistema a seconda dello stato globale del motore (es. in pausa, non vogliamo aggiornare l’animazione).

#### 3.6.3 updateEntity(...)

I passi principali sono i seguenti:
- Se l’entità sta attaccando ```(attack.isAttacking())```, chiamiamo ```handleAttackAnimation(...)``` e interrompiamo l’aggiornamento. L’attacco ha la precedenza, cosicché non si aggiorni l’animazione di camminata durante un fendente.
- Stessa logica per il lancio proiettile: se sta lanciando un proiettile, usiamo ```handleThrowAnimation(...)``` e interrompiamo.
- Se non si sta attaccando o lanciando, l’AnimationSystem deduce l’azione (Action) e la direzione (Direction) correnti in base alla velocità (dx, dy).
- In caso di gravità:
  - dy == 0 → suolo; se dx != 0, Action.WALK, altrimenti Action.IDLE.
  - dy != 0 → aria; se dy > 0, Action.FALL, se dy < 0, Action.JUMP.
  - La direzione orizzontale (RIGHT o LEFT) dipende da dx.
- Se l’animazione è in modalità loop e la nuova coppia (Action, Direction) differisce dalla precedente, resettiamo l’animazione (setCurrentFrame(0), setElapsedTime(0)).
- Altrimenti, chiamiamo updateSpriteFrame(...) per avanzare il frame in base al tempo.
- Se l’entità è una tile map (es. un livello con tile animati), chiamiamo updateTileMapAnimation(...).

---

## 4. Rendering

Il RenderingSystem è un sistema ECS dedicato alla fase di disegno (render) delle entità e degli elementi di gioco.
- Ha una priorità elevata (10), così viene chiamato dopo gli altri sistemi.
- Utilizza, se presente, una Camera per calcolare gli offset di disegno (panning dello scenario).
- Ordina le entità prima di disegnarle, per assicurare un rendering corretto basato su livelli e posizioni.
- Disegna sprite (SpriteComponent) e gestisce il debug di collisioni (ColliderComponent, TileMapComponent).
- Supporta effetti di parallax (ParallaxComponent).

Gli attributi principali sono:
- `camera`: opzionale, se presente fornisce getxOffset() e getyOffset() per lo spostamento della vista.
- `currentXOffset`, `currentYOffset`: memorizzano i valori di offset calcolati dalla camera per il frame corrente.
- `entityComparator`: un Comparator che definisce l’ordine di disegno delle entità, basato su livello (layer) e, in subordine, sulla coordinata Y (per simulare una sorta di “ordine di profondità”).

Il metodo initStateUpdateMap() definisce in quali stati del motore (EngineState) il sistema deve eseguire il proprio lavoro. Viene aggiornato (e quindi può disegnare) per esempio anche nello stato PAUSED, altrimenti il gioco non verrebbe disegnato se in pausa.

Il metodo update(float deltaTime) svolge le seguenti funzioni:
1.	Chiamare updateEntity per ogni entità, per eventuali aggiornamenti di debug (CollisionMapComponent, ColliderComponent).
2.	Aggiornare la camera se non è nulla (camera.update(deltaTime)).

Il rendering vero e proprio avviene nel metodo render(Graphics2D g). Le funzioni principali che svolge sono:
1.	Calcola gli offset della camera (currentXOffset, currentYOffset).
2.	Ordina le entità secondo entityComparator (engine.sortEntities).
3.	Per ogni entità, chiama il metodo renderEntity(entity, g).

Questo meccanismo permette di disegnare le entity in un ordine coerente (per es. entità con layer inferiore disegnate prima, se due entità hanno lo stesso layer si guarda la coordinata Y della parte inferiore delle bounding box).

Il metodo renderEntity(Entity entity, Graphics2D g) disegna elementi specifici in base alle componenti dell’entità:
- SpriteComponent + MotionComponent: viene richiamato `g.drawImage(sc.getCurrentSprite(), (int)(pc.getX() - currentXOffset), (int)(pc.getY() - currentYOffset), null);` per disegnare lo sprite alla posizione (x,y) “traslata” della camera.
- Debug con ColliderComponent: Se engine.isDebug() è true, mostriamo il bounding box del collider colorandolo di rosso semitrasparente. Se colliderComponent.isColliding(), disegniamo anche un overlay giallo con alpha basato su un timer di collisione (timeRemaining).
- AttackComponent: Se c’è un attacco in corso, disegniamo la sua hitBox in verde semitrasparente (questo aiuta a visualizzare la zona d’impatto).
- TileMapComponent:  Disegniamo la mappa (tileMapImage) allineata alla camera: `g.drawImage(tileMapImage, -currentXOffset, -currentYOffset, null)`. In debug mode, individuiamo le tile “solid” e le coloriamo in rosso semitrasparente, oltre a mostrare le collisioni recenti con un overlay giallo.
- ParallaxComponent:  Gestisce disegni di sfondi in parallasse (ad esempio livelli di sfondo che si muovono a velocità ridotta). Usando la velocità di parallass e currentXOffset, si calcola la posizione ripetuta dell’immagine di sfondo.
- ChaseComponent + PathFinder (solo in debug): Se l’entità usa un pathfinder, disegniamo la lista dei nodi (un colore ciano semitrasparente) per mostrare il percorso calcolato.

Il Comparator confronta prima di tutto i layer delle entità (entity.getLayer()). Se diversi, ordina in base a quello. Se uguali, cerca di ottenere MotionComponent e ColliderComponent per calcolare la posizione “finale” in base al bounding box. In sostanza:
1.	e1.getLayer() != e2.getLayer() => confronta i layer.
2.	Se i layer sono uguali, confronta le coordinate y delle parti inferiori delle bounding box per simulare un “sort by feet” (entità con i piedi più in basso disegnate sopra).
3.	Altrimenti restituisce 0 (uguali).

---

## 5. Configurazione del metodo init dello stato di gioco

Per muovere il giocatore sullo schermo di gioco, occorre aggiungere le entity nel metodo `init()` della classe PlayState. Iniziamo creando l’entità “player”:

```java
// Entities:
// Player
Entity player = new Entity(EntityType.PLAYER, 2);
```

Il costruttore riceve un EntityType (PLAYER) e un numero di layer (2), per ordinare il rendering rispetto ad altre entità. Proseguiamo caricando lo sprite del player e istanziando un MotionComponent:

```java
CharacterSpritesheet playerSpritesheet = new CharacterSpritesheet("/sprites/player.sprite");
MotionComponent playerPosition = new MotionComponent(player, 100, 100, 300.0f);
```

- "/sprites/player.sprite" è il file di configurazione dello sprite.
- MotionComponent imposta la posizione iniziale (100,100) e una velocità massima di 300.0f.

Aggiungiamo successivamente i componenti per la gestione dell'input e degli sprite:

```java
player.addComponent(playerPosition);
KeyInputComponent playerInput = new KeyInputComponent(player);
player.addComponent(playerInput);
SpriteComponent playerSprites = new SpriteComponent(player, playerSpritesheet, .08f, true);
player.addComponent(playerSprites);
```

- KeyInputComponent abilita l’entità a reagire ai comandi da tastiera (tramite ActionStateManager).
- SpriteComponent lega l’entità allo spritesheet e definisce la velocità di animazione (.08f) e se l'animazione si ripete (true).

Infine, registriamo l’entità nel nostro stato o motore:

```java
add(player);
```

Aggiungiamo adesso i sistemi, cominciando dalla gestione della mappatura tasti → azioni, creando un InputSystem:

```java
// Systems:
InputSystem inputSystem = new InputSystem(this.engine);
```

Poi eseguiamo il binding delle azioni di base del movimento WASD ai relativi tasti:

```java
inputSystem.bindAction(InputAction.MOVE_LEFT, KeyEvent.VK_A);
inputSystem.bindAction(InputAction.MOVE_RIGHT, KeyEvent.VK_D);
inputSystem.bindAction(InputAction.MOVE_UP, KeyEvent.VK_W);
inputSystem.bindAction(InputAction.MOVE_DOWN, KeyEvent.VK_S);
```

Ora configuriamo due azioni personalizzate, “PAUSE” e “DEBUG”, associando il tasto e il callback da eseguire:

```java
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
```

- “debounced” significa che l’azione si attiva una sola volta quando il tasto viene premuto.
- Se “PAUSE” è attiva, in base allo stato corrente (RUNNING o PAUSED) si richiede il passaggio allo stato opposto.
- “DEBUG” alterna la modalità di debug.

Aggiungiamo poi l’InputSystem alla scena:

```java
add(inputSystem);
```

Dopo l’InputSystem, registriamo gli altri sistemi fondamentali:

```java
add(new MotionSystem(this.engine));
add(new AnimationSystem(this.engine));
add(new RenderingSystem(this.engine));
```

- MotionSystem: aggiorna la posizione delle entità (player, NPC, ecc.) in base alla velocità e ai comandi di input.
- AnimationSystem: gestisce l’avanzamento dei frame nel SpriteComponent (idle, walk, ecc.).
- RenderingSystem: disegna le entità sullo schermo, usando un eventuale offset di camera.

---

## 6. Riepilogo generale

1.	Game Loop: il pannello di gioco (GamePanel) richiama periodicamente engine.update(deltaTime) e engine.render(g).
2.	InputSystem (priorità 1): legge i tasti premuti (KeyboardInputHandler) e aggiorna ActionStateManager. Gestisce anche le azioni personalizzate (PAUSE, DEBUG).
3.	MotionSystem (priorità 2): controlla se i tasti di movimento sono attivi, regola velocità e posizione dell’entità player aggiornando il MotionComponent. Se un entità è un NPC, ne gestisce il path. Se sta attaccando, blocca il movimento.
4.	AnimationSystem (priorità 9): scorre i componenti per la gestione degli sprite, avanzando i frame dell’animazione in base al tempo trascorso. Se un’azione cambia (es. da IDLE a WALK), resetta i frame.
5.	RenderingSystem (priorità 10): calcola l’offset della camera se presente, ordina le entità (layer, Y), e disegna su Graphics2D. Include eventuali debug overlay per collisioni, tile solid, parallax, pathfinding, ecc.

Questa catena consente un flusso coerente: prima input, poi movimento, poi animazione, e infine disegno. L’entità “player”, configurata con KeyInputComponent, MotionComponent e SpriteComponent, si sposterà correttamente in base ai tasti WASD, cambierà animazione (walk/idle) e verrà visualizzata sulla scena.
