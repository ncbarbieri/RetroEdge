# Lezione 1: Introduzione agli ARPG e il Game Loop

## Introduzione alla storia degli ARPG

Gli Action Role-Playing Games (ARPG) sono videogiochi caratterizzati da meccaniche che combinano elementi di combattimento in tempo reale con lo sviluppo di un personaggio. Generalmente, offrono una visuale dall'alto (top-down), consentendo al giocatore di esplorare ambientazioni e combattere contro nemici tramite movimenti omnidirezionali.

### Elementi fondamentali degli ARPG top-down:
- **Visuale Top-down**: il giocatore osserva l'ambiente di gioco dall'alto.
- **Movimento omnidirezionale**: il personaggio può spostarsi liberamente in ogni direzione.
- **Combattimento e interazione dinamica**: azioni rapide e immediate per coinvolgere maggiormente il giocatore.

## Il Game Loop

Il Game Loop è il cuore di ogni videogioco ed è composto da tre fasi principali:

1. **Gestione degli input dell'utente** (tastiera, mouse, controller).
2. **Aggiornamento dello stato di gioco** (posizioni, collisioni, logica).
3. **Rendering della scena aggiornata** sullo schermo.

Questo ciclo si ripete continuamente per tutta la durata del gioco.

## Threading e sincronizzazione

Per garantire fluidità al gioco, spesso si utilizza un thread dedicato per il Game Loop. Il threading consente di separare l'esecuzione delle operazioni di gioco (aggiornamenti e rendering) da quelle del sistema operativo.

```java
@Override
public void run() {
    double drawInterval = 1000000000 / FPS; // intervallo tra ogni frame
    double delta = 0;
    long lastTime = System.nanoTime();
    long currentTime;

    while(gameThread != null) {
        currentTime = System.nanoTime();
        delta += (currentTime - lastTime) / drawInterval;
        lastTime = currentTime;

        if(delta >= 1) {
            update(); // aggiorna la logica del gioco
            repaint(); // richiama il metodo paintComponent
            delta--;
        }
    }
}
```

## Calcolo del Delta Time

Il delta time è il tempo trascorso tra due frame consecutivi e viene utilizzato per aggiornare correttamente le posizioni e animazioni, indipendentemente dalla velocità del sistema su cui gira il gioco.

### Esempio di calcolo del delta time:

```java
long lastTime = System.nanoTime();

while(running) {
    long currentTime = System.nanoTime();
    float deltaTime = (currentTime - lastTime) / 1e9f; // tempo in secondi
    lastTime = currentTime;

    update(deltaTime); // logica aggiornata con deltaTime
    render();
}
```

## Classi di esempio

- **`MainECS.java`**: Classe principale che gestisce inizializzazione e ciclo del gioco.
- **`GamePanel.java`**: Pannello su cui vengono renderizzati gli elementi grafici del gioco.

> Le classi complete sono disponibili nella cartella dei sorgenti del progetto.

