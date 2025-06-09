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
InteractionComponent è un componente che può essere aggiunto a un’entità per definirne il comportamento quando entra in contatto con un’altra.
Il metodo principale è: ```void setOnEntityInteract(Consumer<Entity> callback)```.

Permette di impostare una funzione da eseguire quando l’entità interagisce con un’altra. Il parametro Entity rappresenta l’entità coinvolta nel contatto (tipicamente il giocatore).
L’InteractionSystem controlla ogni frame se c’è una collisione tra un’entità con InteractionComponent e un’altra, e chiama automaticamente il callback se presente.

⸻

## 2. AnimationComponent e AnimationSystem
AnimationComponent gestisce le animazioni sprite del personaggio. Oltre a impostare il tipo di animazione (camminata, attacco, caduta…), fornisce un meccanismo per reagire al termine dell’animazione.
Il metodo chiave è: ```void setOnAnimationEnd(Runnable callback)```.
Permette di definire una funzione da eseguire automaticamente al termine dell’animazione corrente. Questo è fondamentale per orchestrare una sequenza temporale, come una scena di caduta o un’animazione di fine livello.
L’AnimationSystem aggiorna i frame delle animazioni ogni frame. Quando rileva che un’animazione non è in loop e ha raggiunto l’ultimo frame, esegue il callback associato.

⸻

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
    
    // Imposta posizione iniziale e blocca il movimento
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
