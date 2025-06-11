# Lezione 1 – La classe principale MainECS

## Il punto di ingresso: `MainECS.java`

### Obiettivi

- Comprendere il ruolo della classe `MainECS` come entry point del gioco.
- Introdurre il ciclo di avvio: inizializzazione del motore di gioco, set-up della finestra e attivazione degli input.
- Familiarizzare con la gestione della finestra (fullscreen, focus, chiusura).
- Introdurre i primi concetti di ECS (Entity-Component-System) e di come questo si interfaccia con la classe `GamePanel`.
---
### Introduzione

In ogni applicazione Java, l’esecuzione parte dal metodo main(String[] args). In questo progetto, la classe MainECS si occupa di configurare e avviare il motore di gioco, occupandosi del ciclo principale. Le operazioni svolte sono:

1. Inizializzare gli strumenti di input (tastiera e mouse).
2. Creare il motore di gioco (o “engine”), che gestisce logica ed entità.
3. Allestire l’interfaccia grafica creando la finestra principale (`JFrame`).
4. Collegare il pannello di rendering (`GamePanel`) che si occuperà del disegno e del ciclo di gioco.

> Sebbene il nome della classe contenga “ECS”, in questa prima fase ci concentriamo soprattutto sullo scheletro dell’applicazione. Il pattern **Entity-Component-System** verrà approfondito nelle lezioni successive.

---

## Analisi del Codice

### 1. Costante del titolo

```java
private static final String GAME_TITLE = "RetroEdge";
```

È la scritta che appare sulla barra del titolo della finestra. 

### 2. Metodo `main`

```java
public static void main(String[] args) {
    // ...
}
```

È il punto di ingresso dell’intera applicazione Java. All’interno di `main` disponiamo tutta la logica di avvio.

---

### Inizializzazione degli input

```java
KeyboardInputHandler inputHandler = new KeyboardInputHandler();
MouseInputHandler mouseInputHandler = new MouseInputHandler();
```

- Tastiera: `KeyboardInputHandler`
- Mouse: `MouseInputHandler`

---

### Inizializzazione del motore

```java
Engine engine = new GameEngine(keyboardInputHandler, mouseInputHandler);
```

Il motore di gioco (`GameEngine`) coordina:

- Aggiornamenti della logica
- Informazioni di rendering

---

### Creazione del pannello di gioco

```java
GamePanel gamePanel = new GamePanel(engine);
```

---

### Creazione della finestra

```java
JFrame window = createGameWindow(GAME_TITLE, gamePanel, engine);
```

---

### Collegamento degli input

```java
window.addKeyListener(keyboardInputHandler);
window.addMouseListener(mouseInputHandler);
window.addMouseMotionListener(mouseInputHandler);
```

---

### Listener aggiuntivi

```java
addListeners(window, gamePanel, engine);
```

Listener per:

- Focus finestra
- Schermo intero (tasto `F`)
- Chiusura finestra

---

## Approfondimenti

### Fullscreen Mode

```java
GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
```

---

### Focus della finestra

```java
public void windowGainedFocus(WindowEvent e)
```

---

### Chiusura del gioco

```java
window.addWindowListener(new WindowAdapter() {
    public void windowClosing(WindowEvent e) {
        closeGame(...);
    }
});
```

---

# Il cuore grafico: `GamePanel.java`

### Obiettivi

- Comprendere il ruolo del `GamePanel` nel rendering del gioco.
- Introdurre il game loop (aggiornamento + disegno).
- Spiegare la separazione tra logica e rendering (UPS vs FPS).
- Introdurre la gestione dello scaling e del debug.

---

## Introduzione

`GamePanel` estende `JPanel` e implementa `Runnable`, permettendo un game loop in un thread dedicato per migliorare la fluidità.

---

## Costanti e attributi principali

```java
private static final int FPS = 60;
private static final int UPS = 60;
```

```java
private volatile boolean isRunning = true;
private Thread gameThread;
```

---

## Costruttore

```java
public GamePanel(Engine engine)
```

Impostazioni:

- Dimensioni iniziali (es. `640x480`)
- Sfondo nero
- Double buffering
- Listener per il ridimensionamento
- Avvio del thread del game loop

---

## Scaling dinamico

```java
private float scaleX = 1.0f, scaleY = 1.0f;
```

```java
scaleX = (float) panelWidth / GAME_WIDTH;
scaleY = (float) panelHeight / GAME_HEIGHT;
```

---

## Ciclo di gioco (Game Loop)

```java
while (isRunning) {
    // Calcolo tempo
    // update logica
    // rendering
    // debug
    // pausa
}
```

---

## Separazione update/render

```java
if (deltaU >= updateInterval) {
    // logica
}
if (deltaF >= frameInterval) {
    // rendering
}
```

---

## Debug Mode

```java
if (engine.isDebug()) drawDebugInfo(g);
```

```java
g.drawString(ups + " UPS - " + fps + " FPS | " + debugInfo, 10, GAME_HEIGHT - 10);
```

---

## Chiusura del gioco

```java
public void stopGameLoop() {
    isRunning = false;
    gameThread.join();
}
```

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

---

## Focus della finestra

```java
public void windowFocusLost() {
    engine.windowFocusLost();
}
```

---

## Conclusione

- `MainECS.java`: punto di partenza del gioco, setup finestra e input.
- `GamePanel.java`: rendering e logica temporale.

Nella prossima lezione analizzeremo la classe GamePanel, che si occupa del disegno e della gestione del ciclo update/render.

---
