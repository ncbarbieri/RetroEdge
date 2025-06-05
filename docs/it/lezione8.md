# Lezione 8 – NPC con dialoghi interattivi

In questa lezione aggiungeremo al nostro gioco un personaggio non giocante (NPC - non-player character) con le seguenti funzionalità:
- movimento automatico tra due posizioni
- rilevamento della prossimità del giocatore
- notifica visiva quando ci si avvicina
- interazione tramite dialogo


## 1. NPCComponent

NPCComponent consente a un’entità di tipo NPC di:
- muoversi autonomamente verso una posizione target
- calcolare la direzione e la velocità da applicare
- eseguire del codice personalizzato quando ha raggiunto la destinazione

È un componente passivo, ovvero non aggiorna da solo la posizione, ma lavora in sinergia con il MotionSystem.

### 1.1 Attributi principali

| Campo	| Tipo	| Descrizione |
|-------|-------|-------------|
| targetX, targetY	| float	| Coordinate verso cui si sta muovendo l’NPC |
| xSpeed, ySpeed	| float	| Velocità da applicare lungo X e Y |
| onTargetReached	| Runnable	| Callback da eseguire quando il target è raggiunto |

### 1.2 Costruttore

```java
public NPCComponent(Entity entity, float targetX, float targetY)
```

- Inizializza il componente con una destinazione.
- Calcola subito la direzione del movimento con setTargetPosition.

### 1.3 Metodo ```void setTargetPosition(float, float)```

Questo metodo è il cuore del componente. Esegue i seguenti passi:
1.	Memorizza la destinazione (targetX, targetY).
2.	Imposta le velocità xSpeed e ySpeed in modo da puntare esattamente verso la meta.
3.	Per fare questo, calcola:
     - il vettore direzione dalla posizione attuale
     - la norma (modulo) del vettore
     - le componenti normalizzate moltiplicate per la maxSpeed definita nel MotionComponent

Esempio di calcolo:

```java
float dx = targetX - pc.getX();
float dy = targetY - pc.getY();
float mod = sqrt(dx*dx + dy*dy);
xSpeed = dx / mod * movementSpeed;
ySpeed = dy / mod * movementSpeed;
```

Questo approccio garantisce un movimento lineare costante verso la destinazione, indipendente dalla distanza.

### 1.4 Metodo ```void onTargetReached()```

Questo metodo serve a eseguire il codice personalizzato quando il target viene raggiunto. Se è stato registrato un Runnable tramite il metodo ```setOnTargetReached(Runnable onTargetReached)```, viene eseguito:

```java
if (onTargetReached != null) {
    onTargetReached.run();
}
```

Il codice personalizzato è usato tipicamente per:
- impostare una nuova destinazione (es. NPC che va avanti e indietro)
- attivare un’animazione o una reazione dell’NPC
- terminare il movimento

### 1.5 Utilizzo con il MotionSystem

Nel nostro motore di gioco, il movimento delle entità è gestito tramite due elementi principali:
1. MotionComponent: tiene traccia della posizione e della velocità di un’entità.
2. MotionSystem: aggiorna la posizione delle entità in base al tempo trascorso (delta time) e alla loro velocità.

La classe MotionSystem scorre tutte le entità dotate di MotionComponent e aggiorna la loro posizione nel mondo. Mentre il player viene mosso grazie a KeyInputComponent, NPCComponent permette di assegnare a un NPC una destinazione, calcolare la direzione da seguire e aggiornare la velocità di conseguenza. Se l’entità ha un componente NPCComponent, il MotionSystem richiama getXSpeed() e getYSpeed() per aggiornare la velocità del MotionComponent dell’NPC.

Quando l’NPC è vicino abbastanza alla destinazione (entro un certo margine), il sistema:
- imposta le velocità a zero
- chiama onTargetReached()

Questo comportamento è totalmente automatizzato e consente la creazione di NPC reattivi e scriptabili, ad esempio:

```java
npcComponent.setOnTargetReached(() -> {
    // Cambio direzione
    npcComponent.setTargetPosition(newX, newY);
});
```


## 2. ProximityComponent: rilevamento della vicinanza

Il ProximityComponent contiene i dati per consentire a un’entità (tipicamente un NPC) di rilevare la vicinanza di altre entità (es. il giocatore) entro un determinato raggio di interazione. Quando la vicinanza viene rilevata, è possibile:
- attivare uno stato interno
- visualizzare un elemento grafico di notifica (es. un fumetto)

### 2.1 Attributi principali

| Campo	| Tipo	| Descrizione |
|-------|-------|-------------|
| interactionRange	| float	| Raggio entro cui avviene l’interazione. |
| activationFilter	| Predicate<Entity>	| Funzione che definisce chi può attivare la prossimità. |
| isTriggered	| boolean	| Stato corrente: true se almeno una entità è vicina. |
| triggeringEntities	| Set<Entity>	| Insieme delle entità che stanno attivando la prossimità. |
| notificationElement	| UINotification	| Icona grafica associata (es. fumetto). |


### 2.2 Costruttori

- ```public ProximityComponent(Entity entity, float interactionRange)```

  Inizializza con un range e nessun filtro (tutte le entità possono attivarlo).

- ```public ProximityComponent(Entity entity, float interactionRange, Predicate<Entity> activationFilter)```

  Aggiunge anche un filtro che può limitare chi ha il diritto di attivare il componente (es. solo il player).

### 2.3 Metodi principali

- ```public boolean isTriggered()```

  Restituisce lo stato attuale (true se almeno un’entità ha attivato la prossimità).

- ```public void addTriggeringEntity(Entity entity)```

  Aggiunge un’entità tra quelle che stanno attivando il componente. Se supera il filtro (canActivate()), attiva lo stato e mostra la notifica (se presente).

- ```public void removeTriggeringEntity(Entity entity)```

  Rimuove un’entità dal set. Se non rimane più nessuna entità attiva, disattiva il componente e nasconde la notifica.

- ```public void clearTriggeringEntities()```

  Rimuove tutte le entità e resetta lo stato a false.

- ```public boolean canActivate(Entity other)```

  Valuta se una certa entità può attivare la prossimità secondo il filtro. Se non è definito alcun filtro (null), qualsiasi entità è valida.

- ```public void setNotificationElement(UINotification element)```

  Imposta l'elemento di notifica che verrà visualizzato quando la prossimità viene rilevata.

## 3. UINotification

UINotification è una classe che estende UIElement, e che rappresenta un elemento grafico dell’interfaccia utente che segue un’entità (di solito un NPC) e mostra un’animazione ciclica (per esempio un fumetto) quando l’entità è interagibile (es. tramite ProximityComponent).

È usata per guidare visivamente il giocatore, suggerendogli che può attivare un dialogo o altra interazione.

### 3.1 Attributi principali

| Campo	| Tipo	| Descrizione |
|---------|---------|-------------|
| frames	| BufferedImage[]	| Sequenza di immagini da animare. |
| currentFrame	| int	| Indice del frame attualmente visibile. |
| frameDuration	| float	| Durata di ogni frame in secondi. |
| elapsedTime	| float	| Tempo accumulato da quando è stato mostrato l’ultimo frame. |
| trackedEntity	| Entity	| Entità a cui la notifica è ancorata. |
| offsetX/Y	| int	| Spostamento rispetto alla posizione dell’entità (es. per posizionarla sopra la testa). |

### 3.2 Costruttore

```public UINotification(int offsetX, int offsetY, int zIndex, Entity entity, UISpritesheet spritesheet, float frameDuration)```

Imposta:
- la posizione relativa all’entità (offsetX/Y)
- l’entità da seguire (trackedEntity)
- lo zIndex per il disegno in primo piano
- i frame dell'animazione (caricati da uno UISpritesheet)
- la velocità dell’animazione (frameDuration)

Attiva anche l’uso degli offset della camera: ```setUseCameraOffsets(true);```

### 3.2 Metodo ```update(float deltaTime)```

Aggiorna il frame corrente dell’animazione in base al tempo trascorso. L’animazione cicla in automatico ogni periodo di tempo pari a frameDuration secondi:

```java
if (elapsedTime >= frameDuration) {
    currentFrame = (currentFrame + 1) % frames.length;
}
```

### 3.3 Metodo ```render(Graphics2D g, int cameraX, int cameraY)```

Disegna il frame corrente sopra l’entità trackedEntity. Il disegno tiene conto di:
- la posizione assoluta nel mondo (MotionComponent)
- l’offset della camera
- l’offset relativo all'entità tracciata specificato nel costruttore (es. 36 px a destra e -8 px in alto)

### 3.4 Metodo ```resetAnimation()```

Serve per resettare l’animazione alla prima immagine, utile quando l’elemento viene nascosto e poi mostrato di nuovo. Tipicamente chiamato da ```ProximityComponent.setTriggered(false)``` per azzerare l’effetto visivo quando il giocatore si allontana.

## 4 DialogueComponent

Il DialogueComponent collega un’entità di gioco (tipicamente un NPC) a:
- un elemento grafico di dialogo (UIDialogue)
- un componente di input da tastiera (KeyInputComponent)

È pensato per essere attivato esternamente da InteractionSystem quando il giocatore interagisce con l’NPC.

### 4.1 Attributi principali

| Campo	| Tipo	| Descrizione |
|---------|---------|-------------|
| dialogueElement	| UIDialogue	| L’interfaccia visiva del dialogo (es. finestra di testo). |
| keyInput	| KeyInputComponent	| Il componente che riceve input per avanzare nel dialogo. |

### 4.2 Costruttore

```public DialogueComponent(Entity entity, UIDialogue dialogueElement, KeyInputComponent keyInput)```

Associa:
- un NPC (entity)
- un’interfaccia di dialogo (dialogueElement, per esempio UIRotatingDialogue)
- un sistema di input per controllare il flusso del dialogo

Il DialogueComponent non gestisce direttamente la logica del dialogo, ma attiva e disattiva l’interfaccia con metodi semplici.

### 4.3 Metodi principali

- ```startDialogue()```

  Attiva il dialogo, rendendo visibile l’interfaccia.

- ```endDialogue()```

  Nasconde l’interfaccia grafica, chiudendo il dialogo:

- ```getDialogueElement()```

  Restituisce l’oggetto UIDialogue associato

- ```getKeyInputComponent()```

  Restituisce il componente input per monitorare lo stato della tastiera

## 5. UIDialogue

UIDialogue è una classe astratta che estende UIElement e che fornisce un’interfaccia e una logica di base per la gestione di interfacce di dialogo UI.
Definisce le regole generali su:
- ciclo di vita del dialogo
- stato (attivo / terminato)
- aggiornamento e disegno condizionato allo stato
- gestione dell’input utente

È pensata per essere estesa da classi concrete che definiscono:
- come viene mostrato il dialogo
- come si avanza tra le frasi
- come termina il dialogo

### 5.1  Attributi principali

| Campo	| Tipo	| Descrizione |
|---------|---------|-------------|
| state	| DialogueState	| Stato attuale del dialogo: ACTIVE o FINISHED. |
| triggered	| boolean	| Flag per sapere se il dialogo è stato attivato da un evento (opzionale). |

### 5.2 Stato del dialogo

La classe gestisce lo stato con l’enum interno ```DialogueState```:

```java
enum DialogueState {
    ACTIVE,
    FINISHED
}
```

Lo stato controlla:
- se il dialogo deve essere aggiornato o disegnato
- se deve reagire agli input

### 5.3 Metodi astratti da implementare

Le sottoclassi devono implementare i seguenti metodi:

| Metodo	| Scopo |
|---------|-------|
| startDialogue()	| Avvia il dialogo (es. mostra il box e la prima frase). |
| updateDialogue(float)	| Logica per far avanzare o animare il dialogo. |
| renderDialogue(Graphics2D)	| Disegna l’interfaccia del dialogo. |
| handleInput(KeyInputComponent)	| Gestisce l’input da tastiera per avanzare o chiudere. |

### 5.4 Gestione dello stato

Il metodo protetto ```setState(DialogueState newState)``` consente di cambiare lo stato del dialogo. Se lo stato diventa FINISHED viene chiamato il metodo onDialogueFinished(), che può essere sovrascritto nelle sottoclassi per notificare la fine del dialogo al resto del sistema.

## 6. UIRotatingDialogue

UIRotatingDialogue è una classe che estende UIDialogue e rappresenta un’interfaccia grafica di dialogo pensata per:
- visualizzare un insieme di frasi pre-renderizzate una alla volta
- impaginarle su una finestra di dialogo grafica
- gestire il flusso del dialogo tramite input da tastiera
- coordinarsi con lo stato dell’engine (es. bloccare il gioco mentre il dialogo è attivo)

È l’implementazione visiva principale utilizzata per dialoghi statici e scene narrative nel gioco.

### 6.1 Attributi principali

| Campo	| Tipo	| Descrizione |
|---------|---------|-------------|
| dialogueImages	| BufferedImage[]	| Le frasi del dialogo già renderizzate come immagini. |
| currentMessageIndex	| int	| Indice della frase attualmente mostrata. |
| panel	| BufferedImage	| L’immagine di sfondo della finestra di dialogo. |
| textFont	| Font	| Font usato per disegnare il testo. |
| textColor	| Color	| Colore del testo. |
| backgroundColor	| Color	| Colore dello sfondo (sotto il pannello PNG). |
| stateManager	| EngineStateManager	| Permette di gestire lo stato del gioco durante il dialogo. |
| fontMetrics	| FontMetrics	| Misure tipografiche per impaginare correttamente il testo. |

### 6.2 Costruttore

```UIRotatingDialogue(..., String frameFile)```

- Carica lo sfondo grafico da un file (frameFile).
- Calcola posizione e dimensioni del pannello in base alla risoluzione (GamePanel.GAME_WIDTH/HEIGHT).
- Inizializza il font e le metriche per calcolare l’impaginazione delle frasi.

### 6.3 Metodi

```setDialogues(List<String> dialogues)```
- Converte ogni stringa in una immagine bitmap pre-renderizzata (BufferedImage) tramite il metodo renderTextToImage().
- Supporta \\n per la gestione di frasi su più righe.
- Memorizza tutto in un array di immagini che verranno mostrate una alla volta.

```renderDialogue(Graphics2D g, ...)```
- Disegna il rettangolo colorato di sfondo, l’immagine del pannello e la frase attuale.
- Usa gli offset per adattarsi alla posizione della camera se necessario.

```handleInput(KeyInputComponent keyInput)```
- Aspetta che il giocatore prema un tasto associato all’azione "DIALOG" (es. INVIO, Z, ecc.).
- Se premuto, consuma l’azione e imposta il dialogo come finito.

```onDialogueFinished()```
Quando la frase attuale è stata mostrata:
- il dialogo viene chiuso
- lo stato del gioco torna a EngineState.RUNNING
- il messaggio successivo sarà mostrato al prossimo avvio

### 6.4 Dal ProximityComponent all'UIRotatingDialogue

In sintesi, ecco che cosa succede quando viene visualizzato un dialogo: 
1.	L'**InteractionSystem** aggiorna il **ProximityComponent**: modifica il flag isNear a seconda della distanza e attiva o disattiva la notifica **UINotification** di conseguenza.
2.	Se la prossimità viene rilevata e il tasto per mostrare il dialogo risulta premuto, **InteractionSystem** cerca nell'entity il **DialogueComponent** e, se presente, lo attiva.
3.	Il **DialogueComponent** avvia il dialogo tramite **UIDialogue** (in questo caso istanza di **UIRotatingDialogue**), portando lo stato dell'engine a **SHOWING_DIALOGUE** e mostrando la frase corrente.
4.	Quando il giocatore preme di nuovo il tasto per avviare il dialogo, **UIRotatingDialogue** viene nascosto, l'engine torna allo stato **RUNNING** e si avanza alla frase successiva da mostrare alla chiamata seguente.

