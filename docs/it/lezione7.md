# Lezione 7 – Sistema di Interfaccia Utente (UI)

L’interfaccia utente (UI) è la componente visiva che permette al giocatore di ricevere feedback durante la partita, come il numero di gemme raccolte, la salute residua o lo stato del gioco. In questa lezione analizziamo il sistema UI modulare di RetroEdge, basato sull’architettura ECS (Entity-Component-System).

## 1. UIElement – Elemento UI base

La classe UIElement funge da classe base astratta per tutti gli elementi dell’interfaccia utente. È progettata per supportare:
- una gerarchia strutturata di elementi UI (es. pannello contenente etichette e pulsanti),
- coordinate locali e globali,
- gestione opzionale della camera (utile per HUD e UI fissa),
- z-index per la profondità di rendering,
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

## 2. UIComponent – Collegamento tra entità e UI

UIComponent è un componente ECS che consente a un’entità di essere collegata a un UIElement.

Caratteristiche:
- Contiene un riferimento a un UIElement.
- Espone getter e setter per manipolare dinamicamente l’elemento UI.

Note progettuali:
- È un componente passivo: non contiene logica ma solo dati.
- Può essere utilizzato da qualsiasi entità per aggiornare dinamicamente l’interfaccia visiva.

## 3. UISystem – Sistema ECS per aggiornare la UI

UISystem è un sistema ECS che gestisce l’aggiornamento degli elementi UI collegati a entità tramite UIComponent.

Caratteristiche:
- Scorre tutte le entità con UIComponent.
- Richiama update(deltaTime) sull’elemento UI associato.

Note progettuali
- Non disegna nulla: il rendering è centralizzato nel metodo render() dell’Engine.
- Permette agli UIElement di evolvere nel tempo in modo autonomo (ad esempio una barra che si svuota o un’etichetta lampeggiante).

## 4. Label – Etichetta testuale dinamica

Label è una sottoclasse concreta di UIElement, pensata per visualizzare testo sullo schermo.

Caratteristiche:
- Testo dinamico (text): modificabile a runtime.
- Font e colore: completamente personalizzabili.
- Metodo setText(String): per aggiornare il contenuto visualizzato.

Note progettuali
- Utile per punteggi, contatori, notifiche, dialoghi.
- Supporta l’ereditarietà e la gestione del layer per la corretta visualizzazione.

## 5. Architettura completa del sistema UI
| Classe	                  | Ruolo	                                                                 |
|---------------------------|------------------------------------------------------------------------|
| UIElement	                | Classe base per qualsiasi elemento visuale UI	                         |
| Label	                    | UIElement specializzato nella visualizzazione di testo	               |
| UIComponent	              | Componente ECS che collega un’entità a un elemento UI	                 |
| UISystem	                | Sistema ECS che aggiorna ogni elemento UI collegato a entità	         |


## 6. Esempio: visualizzare il numero di gemme raccolte

Vogliamo mostrare in alto a sinistra lo stato delle gemme raccolte dal giocatore. Procediamo in questo modo:

1. Aggiungiamo il font nel metodo init() del PlayState
```java
Font font;
try {
    font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/myFont.otf"));
    font = font.deriveFont(24f);
    GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
} catch (Exception e) {
    font = new Font("Arial", Font.PLAIN, 24);
}
// Register the font
GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
ge.registerFont(font);
```

2. Istanziamo l’etichetta
```java
Label gemLabel = new Label(25, 40, 1, "Gems: 0", font, Color.white);
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
