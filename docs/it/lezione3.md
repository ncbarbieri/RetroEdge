# Lezione 3 – Sprite, Movimento e Animazione

## Obiettivi
- Comprendere come caricare e gestire **sprite** per creare l’animazione del personaggio giocante.  
- Approfondire i **sistemi** di movimento, input, animazione e rendering.  
- Introdurre il **metodo di Eulero semi-implicito** per aggiornare posizione e velocità in modo “realistico”.  
- Mostrare come avviene il **rendering** delle entità animabili.

---

## 1. Introduzione
Finora abbiamo costruito l’**infrastruttura** del gioco con il pattern ECS: entità, componenti, sistemi. Adesso passiamo a **movimento** e **animazione**:  
- **Animazione**: gestire una sequenza di immagini (frame) che si susseguono per dare l’illusione del movimento.  
- **Movimento** “realistico”: aggiornare posizione e velocità di un’entità in base a forze, accelerazioni e input del giocatore.  
- **Rendering**: disegnare il frame corrente dell’animazione sullo schermo, nella posizione corretta.

Quattro sistemi principali ci aiuteranno:
1. **`InputSystem`**: interpreta i comandi dell’utente (tastiera, mouse, gamepad…) e aggiorna i componenti di input o movimento.  
2. **`MotionSystem`**: calcola e applica gli aggiornamenti di posizione usando Eulero semi-implicito.  
3. **`AnimationSystem`**: gestisce i cicli di animazione, passando da un frame all’altro.  
4. **`RenderingSystem`**: disegna effettivamente le entità sullo schermo con le coordinate e i frame adeguati.

---

## 2. Sprite Sheet e Animazioni

### 2.1 Cos’è uno Sprite Sheet
Uno sprite sheet è un’unica immagine che contiene **più frame** di animazione. Ad esempio, potremmo avere un file PNG con tutte le pose del personaggio: idle, camminata, salto, ecc., disposte in griglia.  
Per disegnare il frame corrente, estraiamo (o “clippiamo”) la porzione di immagine corrispondente alla posa desiderata e la stampiamo sullo schermo.

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

- Può interpretare la pressione dei tasti (sinistra, destra, salto…) e modificare un VelocityComponent o AccelerationComponent.
- Se l’entità giocatore possiede un PlayerInputComponent, possiamo distinguere più facilmente chi deve muoversi.

### 4.2 MotionSystem.java

Si occupa della fisica di base e del calcolo della nuova posizione:

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

- Usando VelocityComponent e TransformComponent, applichiamo:

velocity.x += acceleration.x * dt;
velocity.y += acceleration.y * dt;
position.x += velocity.x * dt;
position.y += velocity.y * dt;


- Si possono aggiungere limiti di velocità, gravità, attriti, collisioni semplificate, ecc.

### 4.3 AnimationSystem.java

Gestisce la logica di animazione:

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

	•	L’AnimationComponent potrebbe avere:
	•	currentFrameIndex
	•	timePerFrame (quanto tempo dura un frame)
	•	elapsedTime (quanto tempo è trascorso nel frame corrente)
	•	loop o once (se l’animazione deve ripetersi o no)
	•	A ogni update, se elapsedTime >= timePerFrame, incrementiamo currentFrameIndex e resettiamo elapsedTime.
	•	Possiamo anche gestire stati diversi: ad esempio, “idle”, “walk”, “jump”, ognuno con la sua sequenza di frame.

### 4.4 RenderingSystem.java

L’ultimo anello della catena, si occupa di disegnare l’entità sullo schermo, usando i dati forniti dai componenti di transform e animation:

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
