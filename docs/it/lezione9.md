# Lezione 9: Gestione degli eventi – Dalle interazioni alle cutscene

Nel mondo dei videogiochi, interazioni e cutscene sono strumenti fondamentali per costruire narrazione, atmosfera e gameplay.
Immagina un personaggio che cade in una trappola, perde temporaneamente il controllo e riappare in un’altra area: questo è un esempio di evento interattivo controllato.

Nel nostro motore, un evento di questo tipo si ottiene combinando più componenti e sistemi, ognuno responsabile di una parte dell’azione:
- la rilevazione del contatto tra entità;
- la disattivazione temporanea dell’input;
- l’attivazione di un’animazione;
- lo spostamento in un'altra posizione una volta conclusa l’animazione;
- il ritorno allo stato di gioco.

Questa catena di eventi è alla base non solo delle cutscene interattive, ma di qualsiasi sequenza narrativa o meccanica dinamica nel gioco.

## 1. InteractionComponent e InteractionSystem

L’interazione tra entità (come il giocatore che tocca una trappola o raccoglie un oggetto) è gestita in modo modulare tramite l’accoppiata InteractionComponent + InteractionSystem.

### 1.1 InteractionComponent

Permette a un’entità di reagire a un’interazione (in genere, una collisione con un’altra entità) eseguendo un’azione programmata. È utile per eventi una tantum, puzzle, cutscene o raccolta oggetti.

Campi principali:

- interactable: specifica se l’entità può essere attivata. Utile per disabilitare temporaneamente interazioni.
- onEntityInteract: è una funzione (lambda) che riceve un’entità e definisce cosa succede al momento dell’interazione.

Metodi principali:

| Metodo	| Descrizione |
|-----------|-------------|
| setOnEntityInteract(Consumer<Entity>)	| Imposta l’azione da eseguire quando l’entità viene toccata. |
| interact(Entity other)	| Esegue il comportamento, se l’interazione è attiva (interactable == true). |
| setInteractable(boolean)	| Abilita o disabilita l’interazione in runtime. |
| isInteractable()	| Verifica se l’entità è attualmente attivabile. |

Esempio d’uso:

```java
InteractionComponent pitInteraction = new InteractionComponent(pit);
pitInteraction.setOnEntityInteract(other -> {
    // codice che gestisce l’evento di caduta
});
```

La logica è dichiarativa: ogni entità può avere un comportamento su misura, senza modificare il sistema globale.

### 1.2 InteractionSystem

Si occupa di scansionare ogni frame tutte le entità del gioco, verificare quali si stanno muovendo e se stanno collidendo con oggetti interattivi. Se trova una corrispondenza, attiva il comportamento associato.

Passaggi logici:

1.	Filtra le entità che possono muoversi e collidere (MotionComponent + ColliderComponent).
2.	Confronta ogni coppia con altre entità che abbiano anche InteractionComponent.
3.	Verifica la collisione usando intersects(), basato sui collider.
4.	Se l’entità other è interattiva (isInteractable()), chiama interact(entity), attivando il comportamento.

Metodi di utilità:

| Metodo	| Descrizione |
|-----------|-------------|
| hasMotionAndCollider(e)	| Verifica che un’entità possa muoversi e collidere. |
| hasMotionColliderAndInteraction(e)	| Verifica che l’entità possa essere toccata e interagire. |
| intersects(e1, e2)	| Restituisce true se le AABB delle due entità si sovrappongono. |

Considerazioni progettuali:
- Il sistema è completamente passivo: non definisce alcun comportamento, ma si limita a rilevare condizioni e attivare callback.
- Può essere riutilizzato per ogni tipo di interazione, rendendolo un componente chiave per gameplay reattivo.

## 2. AnimationComponent e AnimationSystem

Il sistema di animazione in un motore ECS deve non solo disegnare il frame corretto in ogni momento, ma anche rilevare la fine di un’animazione per attivare eventi, transizioni, o modifiche di stato. Questa responsabilità è distribuita tra:
- SpriteComponent: memorizza lo stato attuale dell’animazione di un’entità;
- AnimationSystem: aggiorna il tempo e cambia frame, eventualmente attivando callback legati alla fine di un’animazione.

### 2.1 SpriteComponent – Il componente di animazione

Memorizza le informazioni necessarie a gestire una sequenza di frame animati per un’entità, inclusa la possibilità di reagire alla conclusione dell’animazione.

Campi principali:

| Campo	| Significato |
|-------|-------------|
| frames	| Tutti i frame per ogni combinazione Action x Direction. |
| frameDuration	| Tempo da attendere prima di passare al frame successivo. |
| elapsedTime	| Tempo accumulato. Quando supera frameDuration, si avanza al frame successivo. |
| currentFrame	| Indice del frame corrente da visualizzare. |
| looping	| Indica se l’animazione deve ripartire da capo o fermarsi. |
| animating	| Flag per determinare se l’animazione è attiva. |
| onAnimationEnd	| Callback da eseguire quando l’animazione non-looping termina. |
| currentAction / currentDirection	| Specificano l’animazione corrente (es. FALL verso DOWN). |


Metodi rilevanti:

| Metodo	| Funzione |
|-----------|----------|
| setAction(Action a) / setDirection(Direction d)	| Cambiano l’animazione da eseguire. |
| setLooping(boolean)	| Attiva/disattiva il looping. |
| setOnAnimationEnd(Runnable)	| Imposta una funzione da eseguire al termine dell’animazione. |
| update(float deltaTime)	| (chiamato dal sistema) aggiorna elapsedTime, cambia frame e, se necessario, chiama onAnimationEnd. |
| getCurrentFrame()	| Restituisce il frame attuale da disegnare. |

La combinazione setLooping(false) + setOnAnimationEnd() consente di orchestrare eventi temporizzati, per esempio:
- far partire una cutscene solo alla fine di un’animazione;
- aspettare che il personaggio completi un salto prima di riattivare l’input;
- sincronizzare un cambio di stato (teletrasporto, fine livello, esplosione) con la grafica.

Esempio:

```java
playerSprites.setAction(Action.FALL);
playerSprites.setLooping(false);
playerSprites.setOnAnimationEnd(() -> {
    // Teletrasporto dopo la caduta
    playerPosition.setX(1370);
    playerPosition.setY(430);
    engine.getStateManager().requestStateChange(EngineState.RUNNING);
});
```

### 2.2 AnimationSystem – Il sistema di aggiornamento

Il sistema scorre tutte le entità con SpriteComponent e aggiorna:
1.	il tempo dell’animazione (elapsedTime);
2.	il frame corrente da mostrare;
3.	attiva eventuali callback onAnimationEnd quando l’animazione termina.

Dettagli di funzionamento:
1.	Controllo flag animating: evita di aggiornare sprite statici o già terminati.
2.	Avanzamento temporale: accumula elapsedTime e confronta con frameDuration.
3.	Se l’animazione non è in loop e ha raggiunto l’ultimo frame:
   - disattiva animating;
   - esegue il Runnable associato.


## 3. Esempio pratico: la buca (Pit)

Obiettivo

Creiamo una trappola che:
- disabilita l’input del giocatore;
- riproduce una caduta animata;
- riposiziona il personaggio in un’altra area,
- ripristina il controllo.

Codice:

```java
// Entity: Pit
Entity pit = new Entity(EntityType.ITEM, 2);

// Posizione e collider statico
MotionComponent pitPosition = new MotionComponent(pit, 1800, 330, 0f);
pit.addComponent(pitPosition);
ColliderComponent pitCollider = new ColliderComponent(pit, new Rectangle(16, 16), CollisionBehavior.STATIC);
pit.addComponent(pitCollider);

// Componente di interazione
InteractionComponent pitInteraction = new InteractionComponent(pit);
pitInteraction.setOnEntityInteract(other -> {
    // Passa allo stato CUTSCENE
    engine.getStateManager().requestStateChange(EngineState.CUTSCENE);
    
    // Disattiva input del giocatore
    playerInput.setEnabled(false);
    pitInteraction.setInteractable(false);
    
    // Imposta posizione della buca e blocca il movimento
    playerPosition.setX(1736);
    playerPosition.setY(286);
    playerPosition.setVx(0);
    playerPosition.setVy(0);

    // Imposta animazione di caduta
    playerSprites.setFrameDuration(0.5f);
    playerSprites.setLooping(false);
    playerSprites.setAction(Action.FALL);
    playerSprites.setDirection(Direction.DOWN);
    playerSprites.setCurrentFrame(0);
    playerSprites.setElapsedTime(0.0f);

    // Effetto sonoro
    audio.playEffect(1);

    // Al termine dell’animazione...
    playerSprites.setOnAnimationEnd(() -> {
        // Teletrasporta il personaggio
        playerPosition.setX(1370);
        playerPosition.setY(430);

        // Ripristina camminata
        playerSprites.setFrameDuration(0.08f);
        playerSprites.setLooping(true);
        playerSprites.setAction(Action.WALK);
        playerSprites.setDirection(Direction.DOWN);

        // Riabilita interazione e input
        pitInteraction.setInteractable(true);
        playerInput.setEnabled(true);

        // Torna allo stato di gioco normale
        engine.getStateManager().requestStateChange(EngineState.RUNNING);
    });
});
pit.addComponent(pitInteraction);
add(pit);
```

Spiegazione passo passo

| Passaggio	| Comportamento |
|-----------|---------------|
| setOnEntityInteract	| Imposta il comportamento quando il giocatore tocca la trappola. |
| requestStateChange(CUTSCENE)	| Sospende temporaneamente il gioco normale. |
| setEnabled(false)	| Blocca i comandi del giocatore. |
| setAction(FALL)	| Cambia l’animazione. |
| setOnAnimationEnd(...)	| Definisce cosa succede dopo la fine della caduta. |
| requestStateChange(RUNNING)	| Riattiva il normale flusso del gioco. |
