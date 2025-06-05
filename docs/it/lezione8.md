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

UINotification è un elemento grafico dell’interfaccia utente che segue un’entità del mondo di gioco (di solito un NPC) e mostra un’animazione ciclica — come un fumetto o un simbolo — quando l’entità è interagibile (es. tramite ProximityComponent).

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

