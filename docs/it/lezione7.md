# Lezione 7 – Sistema di Interfaccia Utente (UI)

L’interfaccia utente (UI) è la componente visiva che permette al giocatore di ricevere feedback durante la partita, come il numero di gemme raccolte, la salute residua o lo stato del gioco. In questa lezione analizziamo il sistema UI modulare di RetroEdge, basato sull’architettura ECS (Entity-Component-System).

## 1. UIElement – Elemento UI base

La classe UIElement funge da classe base astratta per tutti gli elementi dell’interfaccia utente. È progettata per supportare:
- una gerarchia strutturata di elementi UI (es. pannello contenente etichette e pulsanti),
- coordinate locali e globali,
- gestione opzionale della camera (utile per HUD e UI fissa),
- z-index per l'ordine di rendering,
- input da tastiera e mouse,
- visibilità dinamica.

### 1.1 Coordinate e Posizionamento

| Metodo	| Descrizione |
|--------|-------------|
| getLocalX()/getLocalY()	| Coordinate relative al genitore. |
| getGlobalX()/getGlobalY()	| Coordinate assolute ottenute ricorsivamente sommando le posizioni di tutti i genitori. |
| setX()/setY()	| Imposta la posizione locale. |

Nota: questo approccio è simile al layout ad albero (composite pattern), usato nei moderni sistemi UI (es. JavaFX, Unity UI, DOM HTML).

### 1.2 Gerarchia

| Metodo	| Descrizione |
|--------|-------------|
| setParent(UIElement)	| Imposta il genitore. |
| getParent()	| Restituisce il genitore. |

Uso tipico: consente la composizione di UI complessa, dove un pannello può contenere più elementi figli, e i movimenti del genitore si propagano automaticamente ai figli tramite getGlobalX/Y().


### 1.3 Visibilità

| Metodo	| Descrizione |
|--------|-------------|
| isVisible()	| Ritorna true se l’elemento è visibile. |
| show()/hide()	| Mostra o nasconde l’elemento. |

Utile per mostrare/nascondere temporaneamente menu, popup, ecc.

### 1.4 Profondità: zIndex

| Metodo	| Descrizione |
|--------|-------------|
| getZIndex()	| Priorità di disegno. |
| setZIndex(int)	| Modifica la priorità. |

Gli elementi UI con zIndex maggiore sono disegnati sopra a quelli con zIndex inferiore. È cruciale per gestire l’ordine visivo (es. finestre sovrapposte).

### 1.5 Integrazione con la camera

| Metodo	| Descrizione |
|--------|-------------|
| usesCameraOffsets()	| Indica se le coordinate devono tener conto della camera. |
| setUseCameraOffsets()	| Abilita/disabilita questa modalità. |

Utile per distinguere:
- **UI fissa**: elementi HUD che non si muovono con il mondo (useCameraOffsets = false)
- **UI ancorata al mondo**: etichette su NPC o oggetti (useCameraOffsets = true)

### 1.6 Gestione dell’input

| Metodo	| Descrizione |
|--------|-------------|
| handleInput(KeyInputComponent)	| Override opzionale per gestire input da tastiera. |
| handleMouseInput(MouseInputHandler)	| Override opzionale per gestire input da mouse. |

Questi metodi sono vuoti per default, ma possono essere ridefiniti nelle sottoclassi per implementare UI interattiva (es. bottoni cliccabili, hotkey, menu navigabili).

### 1.7 Metodi astratti da implementare

| Metodo	| Descrizione |
|--------|-------------|
| update(float deltaTime)	| Aggiorna lo stato dell’elemento (animazioni, timer, input ecc.) |
| render(Graphics2D g, int cameraX, int cameraY)	| Disegna l’elemento, tenendo conto eventualmente della camera. |

L’interfaccia render prevede parametri cameraX e cameraY per decidere dinamicamente se compensare con l’offset della camera.

## 2. UIGroup – Contenitore gerarchico di elementi UI

UIGroup è una sottoclasse concreta di UIElement progettata per raggruppare logicamente più elementi UI. Funziona da contenitore padre per altri UIElement, applicando trasformazioni comuni come posizione, visibilità e ordine di rendering.

### 2.1 Comportamento principale

| Caratteristica	| Descrizione |
|-------------------|-------------|
| Contiene figli	| Tiene una lista ordinata di elementi UIElement. |
| Posizionamento gerarchico	| Ogni figlio eredita le coordinate globali dalla posizione del gruppo. |
| Ordinamento per zIndex	| I figli vengono ordinati per zIndex crescente al momento dell’aggiunta. |
| Propagazione update()	| L’aggiornamento dei figli avviene automaticamente. |
| Propagazione render()	| Il disegno di tutti i figli viene effettuato ricorsivamente. |
| Propagazione input()	| Gli eventi di tastiera e mouse vengono inoltrati ai figli. |

### 2.2 Metodi essenziali

1. addChild(UIElement child)
    - Imposta il gruppo come genitore del figlio.
    - Aggiunge il figlio alla lista children.
    - Riordina i figli secondo il loro zIndex.
2. removeChild(UIElement child)
   - Rimuove il figlio dal gruppo e gli cancella il riferimento al genitore.
3. clearChildren()
   - Rimuove tutti i figli, utile per smontare l’interfaccia o aggiornare dinamicamente il contenuto (es. menu a scelta).

### 2.3 Override dei metodi di UIElement

| Metodo	| Funzione |
|-----------|----------|
| render(Graphics2D, xOffset, yOffset)	| Propaga il disegno a tutti i figli visibili. |
| update(float deltaTime)	| Aggiorna tutti i figli attivi. |
| handleInput(KeyInputComponent)	| Inoltra gli input da tastiera a ogni figlio visibile. |
| handleMouseInput(MouseInputHandler)	| Inoltra gli input da mouse a ogni figlio visibile. |

Tutte queste operazioni sono condizionate dalla visibilità del gruppo: se il gruppo è nascosto (!isVisible()), i figli non vengono aggiornati né disegnati.

### 2.4 Integrazione con UIElement: composizione strutturale

L’introduzione di UIGroup realizza una composizione ad albero tra elementi dell’interfaccia. Ogni UIElement può avere un parent, e un UIGroup può fungere da contenitore e gestore automatico di tutti i figli.

Questa struttura è estremamente potente per:
- costruire interfacce complesse e modulari (es. finestre, popup, HUD con indicatori multipli),
- centralizzare l’aggiornamento e il rendering a partire da un nodo padre,
- traslare o nascondere l’interfaccia intera con una singola operazione sul UIGroup.

### 2.5 Esempi d’uso tipici

| Caso d’uso	| Struttura UI suggerita |
|---------------|------------------------|
| Menu principale	| Un UIGroup contenente etichette e pulsanti. |
| Barra superiore HUD	| Un UIGroup ancorato in alto con vita, gemme. |
| Dialogo con NPC	| Un UIGroup con sfondo, nome, e messaggio. |

## 3.  UILabel – Etichetta testuale per l’interfaccia utente

La classe Label è una specializzazione di UIElement pensata per visualizzare una stringa di testo a schermo. È utilizzata per messaggi statici o dinamici, come:
- il numero di gemme o vite,
- dialoghi,
- titoli di schermate,
- indicazioni di stato (es. “Game Paused”).

### 3.1 Attributi principali

| Attributo	| Tipo	| Descrizione |
|-----------|-------|-------------|
| text	| String	| Il contenuto testuale visualizzato dalla label. |
| font	| Font	| Il font usato per il rendering. |
| color	| Color	| Il colore del testo. |

Questi tre parametri sono completamente personalizzabili al momento della creazione o dinamicamente.

### 3.2 Metodi

```java
public UILabel(int x, int y, int zIndex, String text, Font font, Color color)
```
Descrizione del comportamento del costruttore:
- Inizializza la posizione, priorità di disegno (zIndex) e proprietà visive.
- Imposta useCameraOffsets a false per default static UI, evitando che il testo si muova con la telecamera (adatto per HUD, menu, titoli).

```java
render(Graphics2D g2d, int cameraX, int cameraY)
```
Descrizione del comportamento:
1.	Controllo visibilità: se !isVisible(), il metodo termina subito.
2.	Impostazione font e colore.
3.	Calcolo della posizione finale:
     - Basata su getGlobalX/Y(), quindi supporta la gerarchia.
     - Applica gli offset della camera solo se richiesto tramite usesCameraOffsets().
4.	Disegno del testo con drawString().

Questo comportamento rende Label perfettamente compatibile con UIGroup e con sistemi a layout gerarchico.

```java
update(float deltaTime)
```

Il metodo è vuoto, perché le Label non necessitano di animazioni o aggiornamenti interni per default. Il metodo resta definito per rispettare le specifiche di UIElement e può essere ridefinito in future estensioni (es. blinking, animazioni di comparsa, ecc.).

```java
getText()	
```
Restituisce il testo attuale.

```java
setText(String)	
```
Modifica il contenuto della label e viene usato per aggiornamenti dinamici del testo (es. punteggio, stato, dialogo).

## 4. UIComponent – Collegamento tra entità e interfaccia utente

UIComponent è un componente ECS che lega un’entità a un elemento dell’interfaccia grafica (UIElement). Consente di:
- aggiornare l’elemento UI in risposta agli eventi dell’entità,
- inoltrare input all’elemento UI associato,
- mantenere l’interfaccia sincronizzata con lo stato del gioco.

Caratteristiche:
- Contiene un riferimento a un UIElement.
- Espone getter e setter per manipolare dinamicamente l’elemento UI.

### 4.1 Costruttore

```java
public UIComponent(Entity entity, UIElement uiElement)
```

- Registra l’entità proprietaria tramite super(entity).
- Collega direttamente l’elemento UI da gestire.

L'uso dell’entity consente di mantenere coerenza nel sistema ECS, pur trattandosi di un componente grafico.

### 4.2 Metodi

```java
getUIElement() / setUIElement()
```
- Getter e setter standard per l’elemento UI.
- Permettono dinamicamente di sostituire l’elemento grafico associato (utile per cambi di interfaccia, aggiornamenti, ecc.).

```java
handleInput(KeyInputComponent keyInput)
```
- Inoltra l’input da tastiera all’elemento UI associato, se presente.
- Il metodo richiama uiElement.handleInput(...), sfruttando il supporto polimorfico per la gestione dell’input.

Questo permette di intercettare input specifici (es. hotkey per aprire un pannello, attivare un elemento visibile solo a certe condizioni).

### 4.3 Relazioni con altri componenti

| Relazione	| Componente / Classe	| Descrizione |
|-----------|-----------------------|-------------|
| In aggregazione	| UIElement	| Elemento visuale da aggiornare/renderizzare |
| Parte di entità	| Entity	| L’elemento grafico è collegato logicamente a un’entità ECS |
| Usato da sistema	| UISystem	| Il sistema UI scorre le entità con UIComponent e aggiorna/renderizza i rispettivi elementi |

## 5. UISystem – Sistema ECS per la gestione dell’interfaccia utente

UISystem è un sottosistema di BaseSystem responsabile dell’aggiornamento e del rendering di tutti gli elementi UI associati a entità tramite UIComponent. Opera esclusivamente in stati del motore in cui l’interfaccia utente deve essere visibile o aggiornata (es. durante il gioco attivo o nelle cutscene).

## 5.1 Architettura e Comportamento

Attributi principali:

| Campo	| Tipo	| Descrizione |
|-------|-------|-------------|
| camera	| Camera	| Riferimento alla camera per ottenere gli offset di rendering. |
| currentXOffset	| int	| Offset orizzontale della camera (usato per elementi che si muovono col mondo). |
| currentYOffset	| int	| Offset verticale della camera. |

Gli offset permettono di sincronizzare il rendering degli UIElement che richiedono il follow della telecamera (es. etichette su NPC).

### 5.2 Metodi

Nel costruttore viene impostata la priorità a 11, suggerendo che il sistema UI viene renderizzato dopo il mondo e gli sprite, in primo piano. Nel metodo `initStateUpdateMap()` viene definito in quali stati del motore (EngineState) il sistema deve essere attivo (cioé RUNNING, CUTSCENE, SHOWING_DIALOG).

```java
update(float deltaTime)
```

Metodo centrale per l’aggiornamento logico dell’interfaccia.

Fasi principali:
1.	Accesso sicuro alle entità con UIComponent (tramite engine.accessEntitiesWithComponent).
2.	Per ciascuna entità:
     - Se presente KeyInputComponent, inoltra l’input all’elemento UI.
     - Richiama update(deltaTime) sull’elemento UI associato.

La gestione dell’input è opzionale e deferita all’elemento UI, permettendo il supporto a etichette interattive, menu, bottoni, ecc.

```java
render(Graphics2D g)
```

Metodo per il disegno degli elementi UI a schermo.

Fasi principali:
1.	Calcola gli offset cameraX, cameraY.
2.	Scorre le entità con UIComponent.
3.	Se l’elemento UI è visibile (isVisible()):
     - Chiama render(g, cameraX, cameraY).

L’uso dell’offset consente agli UIElement di decidere se compensare o meno la camera, in base al flag useCameraOffsets.

## 6. Architettura completa del sistema UI
| Classe	                  | Ruolo	                                                                 |
|---------------------------|------------------------------------------------------------------------|
| UIElement	                | Classe base per qualsiasi elemento visuale UI	                         |
| UIGroup	                | Classe base per raggruppare logicamente più elementi UI	                         |
| UILabel	                    | UIElement specializzato nella visualizzazione di testo	               |
| UIComponent	            | Componente ECS che collega un’entità a un elemento UI	                 |
| UISystem	                | Sistema ECS che aggiorna ogni elemento UI collegato a entità	         |


## 7. Esempio: visualizzare il numero di gemme raccolte

Vogliamo mostrare in alto a sinistra lo stato delle gemme raccolte dal giocatore. Procediamo in questo modo:

1. Aggiungiamo il font nel metodo init() del PlayState
```java
Font font;
try {
    font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/HyliaSerifBeta-Regular.otf"));
} catch (Exception e) {
	font = new Font("Arial", Font.PLAIN, 40);
}
font = font.deriveFont(24f); // Set font size

// Register the font
GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
ge.registerFont(font);
```

2. Istanziamo l’etichetta
```java
UILabel gemLabel = new UILabel(25, 40, 1, "Gems: 0", font, Color.white);
gemLabel.show();
```

3. Colleghiamo l'etichetta a un’entità con UIComponent
```java
Entity uiPlayer = new Entity(EntityType.UI, 6);
UIComponent pComponent = new UIComponent(uiPlayer, gemLabel);
uiPlayer.addComponent(pComponent);
add(uiPlayer);
```

4. Quando il giocatore raccoglie una gemma, modifichiamo il testo della label modificando il codice dell'InteractionComponent
```java
gemLabel.setText("Gems: " + playerComponent.getGems());
```

5. Aggiungiamo il sistema per l'aggiornamento dell'interfaccia utente
```java
add(new UISystem(this.engine, camera));
```
