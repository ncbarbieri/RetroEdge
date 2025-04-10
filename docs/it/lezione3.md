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

#### 2.4.2 Inizializzazione finale

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

### 3.7 Flusso Completo

1. Game Loop chiama engine.update(dt) → AnimationSystem.update(dt):
   - AnimationSystem scorre le entità con SpriteComponent.
   - Per ogni componente, chiama updateAnimation(dt).
   - Se elapsed >= frameTime, incrementa currentFrameIndex.
2. Game Loop chiama engine.render(g2D) → RenderingSystem.render(g2D):
   - RenderingSystem scorre le entità con SpriteComponent.
   - Chiama spriteComp.getCurrentFrame() per ottenere la BufferedImage attuale.
   - Disegna l’immagine alla posizione dell’entità.

