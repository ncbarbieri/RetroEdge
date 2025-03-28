# Lezione 1

# Il punto di ingresso: `MainECS.java`

## Obiettivi

- Comprendere il ruolo della classe `MainECS`.
- Introdurre il ciclo di avvio del gioco.
- Familiarizzare con la creazione della finestra di gioco.
- Introdurre input da tastiera e mouse.
- Spiegare il passaggio alla modalità fullscreen.

---

## Introduzione

Ogni programma Java inizia con un metodo `main`. Nei nostri giochi, la classe `MainECS.java` rappresenta il punto di partenza del progetto: qui viene inizializzato tutto ciò che serve per avviare il gioco.

---

## Analisi del Codice

### Costante del titolo

```java
private static final String GAME_TITLE = "Game Tutorial";
```

Definiamo il titolo che verrà visualizzato nella barra della finestra del gioco.

---

### Metodo `main`

```java
public static void main(String[] args) {
```

Questo è il punto di ingresso dell’intera applicazione.

#### Inizializzazione degli input

```java
InputHandler inputHandler = new InputHandler();
MouseInputHandler mouseInputHandler = new MouseInputHandler();
```

Creiamo due gestori di input:
- Tastiera (`InputHandler`)
- Mouse (`MouseInputHandler`)

#### Inizializzazione del motore

```java
Engine engine = new GameEngine(inputHandler, mouseInputHandler);
```

Il motore di gioco (`GameEngine`) coordina gli aggiornamenti della logica e il disegno su schermo.

#### Creazione del pannello di gioco

```java
GamePanel gamePanel = new GamePanel(engine);
```

`GamePanel` è il pannello grafico che verrà aggiunto alla finestra e gestisce il rendering.

#### Creazione della finestra

```java
JFrame window = createGameWindow(GAME_TITLE, gamePanel, engine);
```

Chiamiamo un metodo di utilità per creare la finestra principale del gioco (`JFrame`), con titolo e pannello di gioco.

#### Collegamento degli input

```java
window.addKeyListener(inputHandler);
window.addMouseListener(mouseInputHandler);
window.addMouseMotionListener(mouseInputHandler);
```

Connettiamo tastiera e mouse alla finestra di gioco.

#### Listener aggiuntivi

```java
addListeners(window, gamePanel, engine);
```

Aggiungiamo listener per:
- Focus della finestra (pausa/continua)
- Schermo intero (tasto **F**)
- Chiusura della finestra (ferma tutto)

---

## Approfondimenti

### Fullscreen Mode

```java
GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
```

Utilizziamo le API di Java per passare da finestra a schermo intero e viceversa, premendo **F**.

### Focus della finestra

```java
public void windowGainedFocus(WindowEvent e)
```

Gestiamo l'evento in cui la finestra torna in primo piano. Utile per mettere in pausa il gioco se il giocatore cambia finestra.

### Chiusura del gioco

```java
window.addWindowListener(new WindowAdapter() {
    public void windowClosing(WindowEvent e) {
        closeGame(...);
    }
});
```

Alla chiusura:
- Ferma il `GamePanel`
- Libera le risorse del motore
- Chiude il programma in sicurezza

---

## Esercizio Proposto

1. Cambia il titolo della finestra in `"My First Game!"`
2. Aggiungi un messaggio di log quando il gioco passa alla modalità a schermo intero.
3. Premi **F** durante l’esecuzione per testare il comportamento fullscreen.
4. Aggiungi un commento a ogni blocco della funzione `main`.

---

# Il cuore grafico: `GamePanel.java`

## Obiettivi

- Comprendere il ruolo del `GamePanel` nel rendering del gioco.
- Introdurre il concetto di game loop.
- Spiegare il rendering e l'aggiornamento separati.
- Introdurre la gestione dello scaling e del debug.

---

## Introduzione

`GamePanel` è il pannello principale che gestisce **il disegno del gioco** su schermo e **il ciclo di gioco** (o **game loop**). Estende `JPanel` e implementa `Runnable`, permettendo così di eseguire aggiornamenti e disegni in un thread separato.

---

## Costanti e attributi principali

```java
private static final int FPS = 60;
private static final int UPS = 60;
```

Definiamo due obiettivi:
- **FPS**: frame per secondo (quanto spesso viene disegnato lo schermo).
- **UPS**: update per secondo (quanto spesso viene aggiornata la logica di gioco).

```java
private volatile boolean isRunning = true;
private Thread gameThread;
```

Queste variabili gestiscono l’esecuzione del ciclo di gioco in un thread separato.

---

## Costruttore

```java
public GamePanel(Engine engine)
```

Inizializza il pannello:

- Imposta la dimensione preferita (`640x480`)
- Imposta sfondo nero
- Attiva il double buffering (per evitare sfarfallii)
- Aggiunge un listener per il **ridimensionamento**
- Avvia il **game loop**

---

## Scaling dinamico

```java
private float scaleX = 1.0f, scaleY = 1.0f;
```

Viene usato per adattare il rendering alla dimensione della finestra, in modo proporzionale.

Il metodo `updateScale()` calcola i nuovi valori di scala quando la finestra viene ridimensionata:

```java
scaleX = (float) panelWidth / GAME_WIDTH;
scaleY = (float) panelHeight / GAME_HEIGHT;
```

---

## Ciclo di gioco (Game Loop)

Il game loop rappresenta l’insieme delle operazioni svolte per generare ogni singolo frame: l’aggiornamento dello stato del gioco e il rendering, cioè la generazione del frame con i dati aggiornati. Nella moderna programmazione dei videogiochi, si preferisce dividere la frequenza degli update del gioco (ups, update per second) dalla frequenza di rendering (fps, frame per second), per esempio per poter aggiornare il gioco a una frequenza maggiore rispetto a quella di generazione dei frame.
Il cuore della classe è nel metodo `run()`:

```java
while (isRunning) {
    // 1. Calcolo del tempo trascorso
    // 2. Aggiornamento logica di gioco (UPS)
    // 3. Rendering (FPS)
    // 4. Debug
    // 5. Breve pausa per evitare uso eccessivo della CPU
}
```

### Separazione update/render

```java
if (deltaU >= updateInterval) updateGame(...);
if (deltaF >= frameInterval) repaint();
```

- La **logica** (movimenti, interazioni) viene aggiornata con frequenza `UPS`.
- Il **rendering** avviene separatamente con frequenza `FPS`.

---

## Debug Mode

```java
if (engine.isDebug()) drawDebugInfo(g);
```

Quando è attivo il debug, viene mostrata in sovrimpressione la velocità attuale in **UPS** e **FPS**, oltre a informazioni fornite dal motore.

Il disegno avviene in basso a sinistra:

```java
g.drawString(ups + " UPS - " + fps + " FPS | " + debugInfo, 10, GAME_HEIGHT - 10);
```

---

## Chiusura del gioco

```java
public void stopGameLoop() {
    isRunning = false;
    gameThread.join(); // Aspetta la fine del thread
}
```

Questo metodo viene richiamato quando la finestra si chiude, per terminare correttamente il ciclo di gioco.

---

## Rendering

```java
@Override
public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;
    g2D.scale(scaleX, scaleY);
    engine.render(g2D);
}
```

Ogni volta che `repaint()` è chiamato, il pannello viene ridisegnato:
- Si applica lo **scaling**
- Si delega il disegno al motore di gioco

---

## Focus della finestra

```java
public void windowFocusLost() {
    engine.windowFocusLost();
}
```

Questi metodi vengono chiamati quando la finestra perde o guadagna il focus (ad esempio, quando l’utente passa a un’altra finestra), utili per mettere in pausa il gioco.

---

## Esercizi

1. Cambia le dimensioni iniziali del gioco a 800x600.
2. Aggiungi un messaggio di log nel metodo `updateGame()` per vedere ogni quanto viene chiamato.
3. Prova ad attivare e disattivare il debug e osserva la differenza.
4. Ridimensiona la finestra e verifica che il gioco venga ridisegnato correttamente con lo scaling.

---

## Conclusione

La classe `MainECS.java` è il cuore dell’inizializzazione del gioco. Imposta l’ambiente, collega input e grafica, e prepara tutto per il gameplay. 
`GamePanel` è il fulcro visivo e temporale del nostro motore. Si occupa del rendering, gestisce il ciclo di aggiornamento del gioco, e si adatta a ogni dimensione della finestra.

---
