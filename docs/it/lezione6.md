# Lezione 6: Interazione con gli oggetti e gestione dell'audio

**Obiettivi della lezione**:
- Implementare meccaniche di interazione nel mondo di gioco.
- Creare oggetti raccoglibili generici.
- Gestire attributi specifici del giocatore.
- Gestire musica ed effetti sonori

## 1. Introduzione all’interazione con gli oggetti

Un gioco diventa più coinvolgente quando offre possibilità di interazione con oggetti o personaggi ed è accompagnato da musica ed effetti sonori. In questa lezione impareremo a introdurre meccaniche interattive, come la raccolta di oggetti utili, e a riprodurre musica ed effetti sonori.

Le classi coinvolte sono:
- InteractionComponent
- CollisionSystem
- InteractionSystem
- PlayerComponent
- AudioManager

## 2. InteractionComponent

La classe InteractionComponent permette alle entità di reagire dinamicamente alle interazioni nel mondo di gioco, sia con altre entità che con specifiche zone o tile della mappa. È progettata per fornire una grande flessibilità nel definire logiche differenti per ciascun tipo di interazione.

Attributi:

InteractionComponent utilizza principalmente le seguenti strutture dati e tipi funzionali:
- `Boolean interactable`: Indica se l’entity può interagire o meno. Permette di abilitare o disabilitare le interazioni dinamicamente.
- `Consumer onEntityInteract`: Il comportamento del Consumer viene descritto da una funzione lambda che accetta come parametro un oggetto di tipo Entity. Quando un’entità entra in interazione con un’altra, questo metodo definisce la logica da eseguire.
- `Consumer<Set> onTileInteract`: È una funzione che accetta come parametro un insieme (Set) di nodi (Node). Quando l’entità interagisce con tiles specifiche della mappa, questo metodo viene invocato per gestire l’interazione.
- `Set interactionSet`: Un insieme utilizzato per mantenere un elenco di entità che hanno interagito (o stanno interagendo) con questa entità. Essendo un Set, assicura l’unicità degli elementi evitando duplicati.
- `Set collisionTiles`: Un insieme che memorizza i nodi (o tiles) specifici che stanno interagendo (sono in collisionne) con l’entità corrente. Permette una gestione flessibile e precisa delle collisioni con il terreno.

Metodi principali e loro utilizzo:
- `InteractionComponent(Entity entity)`: Costruttore che inizializza tutte le strutture dati interne, impostando interactable a true.
- `boolean isInteractable()` / `void setInteractable(boolean interactable)`:  Consentono di abilitare o disabilitare l’interattività dell’entità.
- `void setOnEntityInteract(Consumer<Entity> onEntityInteract)` / `void entityInteract(Entity other)`: Questi metodi definiscono e attivano la logica per gestire l’interazione con un’altra entità.
- `void setOnTileInteract(Consumer<Set<Node>> onTileInteract)` / `void tileInteract(Set<Node> tiles)`: Permettono di definire ed eseguire logiche personalizzate quando l’entità entra in contatto con determinate tiles della mappa.
- `Set<Entity> getInteractionSet()` / `void clearInteractions()`:  Utili per tracciare quali entità hanno interagito con l’entità corrente, permettendo di mantenere uno stato di interazioni per una eventuale gestione differenziata o pulizia periodica.
- `Set<Node> getCollisionTiles()` / `void addCollisionTile(Node node)` / `void addCollisionTiles(Set<Node> nodes)` / `void clearCollisionTiles()`: Consentono una gestione granulare dei nodi coinvolti nelle collisioni, facilitando comportamenti basati su specifiche aree o posizioni della mappa.

Punti chiave:
- **Flessibilità funzionale**: Utilizzando l’interfaccia funzionale Consumer, InteractionComponent permette di definire facilmente e in modo personalizzato le logiche da eseguire al verificarsi di interazioni diverse, rendendo la gestione degli eventi chiara e pulita.
- **Separazione di responsabilità**: L’uso distinto di metodi e insiemi per le interazioni con entità (Entity) e tiles (Node) mantiene il codice modulare e ordinato.
- **Gestione dinamica**: La possibilità di abilitare/disabilitare dinamicamente l’interattività di un’entità offre flessibilità nella progettazione delle meccaniche di gioco.
- **Efficienza nella gestione delle interazioni**: Usare strutture come HashSet garantisce un controllo rapido delle interazioni evitando duplicati e consentendo una gestione efficiente delle collisioni.

## 3. CollisionSystem

Il CollisionSystem svolge un ruolo cruciale nella rilevazione e gestione delle interazioni fra entità e tra entità e tiles della mappa. Sebbene il suo compito primario sia la risoluzione delle collisioni fisiche, esso identifica anche le collisioni come eventi interattivi, notificando queste informazioni all’InteractionComponent.

L’interazione avviene principalmente in due contesti:
1. Collisioni tra Entità e Tilemap: Quando un’entità collide con una tile solida, il sistema identifica le tile coinvolte e le registra tramite l’InteractionComponent. Punti chiave:
    - Set di nodi (Node): il sistema usa un `Set<Node>` per raccogliere le tiles con cui l’entità è in collisione.
    - Aggiornamento dinamico: Il set viene aggiornato ogni frame, assicurando che l’InteractionComponent abbia sempre informazioni aggiornate.
2. Collisioni tra Entità: Il sistema gestisce anche l’interazione tra entità diverse. Punti chiave:
    - Mutua registrazione: ogni entità registra l’altra, permettendo una gestione bidirezionale dell’interazione.
    - Gestione del set di interazioni: Anche qui, un `Set<Entity>` viene aggiornato dinamicamente per tracciare tutte le entità coinvolte.

Metodi rilevanti per interazioni:
- `resolveTileCollisions()`: Raccoglie e registra automaticamente le tile interagenti, aggiornando l’InteractionComponent.
- `handleCollision()`: Rileva collisioni tra entità e aggiorna immediatamente l’insieme delle entità coinvolte.
- `findCollisionsInRect()` e `canMove()`: Supportano la rilevazione dettagliata delle tiles specifiche coinvolte nella collisione, rendendo possibile identificare esattamente quali aree della mappa interagiscono con un’entità.

Strutture dati e gestione interazioni:
- Set di nodi (`Set<Node> collisions`): utilizzato per identificare rapidamente tutte le tiles coinvolte in un’interazione con entità.
- Aggiornamento coerente e sicuro: I set vengono svuotati (clear) e riempiti (add o addAll) ogni ciclo di aggiornamento, mantenendo sempre informazioni fresche e accurate.

Vantaggi:
- Integrazione fluida con l’InteractionComponent: Il CollisionSystem delega alla struttura InteractionComponent la gestione effettiva delle logiche interattive, limitandosi a fornire informazioni accurate sugli eventi di collisione/interazione.
- Modularità e chiarezza: I due tipi di interazione (entity-tile e entity-entity) sono ben distinti e gestiti separatamente, mantenendo il codice ordinato e comprensibile.
- Performance: L’uso dei Set garantisce che l’informazione sulle interazioni sia registrata in modo efficiente, senza duplicati e con accesso rapido.
- Flessibilità: Il sistema permette facilmente di aggiungere nuove tipologie di interazioni semplicemente implementando ulteriori logiche basate sui set già esistenti.

Il CollisionSystem, in sintesi, identifica e raccoglie informazioni dettagliate sulle collisioni e le passa al componente InteractionComponent. Questo approccio permette di separare chiaramente la responsabilità della rilevazione e risoluzione delle collisioni fisiche da quella della gestione della logica interattiva, consentendo così un’architettura di gioco robusta, efficiente e facilmente espandibile.

## 4. InteractionSystem

InteractionSystem gestisce in modo centralizzato tutte le interazioni che avvengono tra le entità nel gioco, occupandosi sia di interazioni dirette (entity-entity) sia delle interazioni tra entità e tiles della mappa di gioco. Questo sistema controlla e risponde dinamicamente agli eventi di interazione e prossimità, avviando dialoghi, raccogliendo oggetti o attivando meccanismi di gioco.

La logica di interazione è suddivisa in due fasi principali:

1. Elaborazione delle interazioni:
    - Cicla su tutte le entità con un InteractionComponent.
    - Chiama il metodo `handleInteraction()` per ciascuna entità, gestendo le interazioni accumulate durante il ciclo.
2. Gestione delle interazioni basate su prossimità (che verranno approfondite successivamente):
    - Per ogni entità dotata di ProximityComponent, verifica se altre entità entrano nel raggio di prossimità stabilito.
    - Usa `updateEntityPair()` per aggiornare dinamicamente lo stato delle interazioni basate sulla vicinanza tra coppie di entità.

Il metodo `handleInteraction(Entity entity, float deltaTime)` gestisce per ogni entity due tipi di interazione:
1.	Entity-Entity:
     - Utilizza il set di entity che hanno interagito (interactionSet) dell'InteractionComponent.
     - Esegue l’interazione definita tramite entityInteract() e poi ripulisce il set.
2.	Entity-Tile:
     - Gestisce i tiles in collisione registrati in collisionTiles.
     - Invoca la logica definita in tileInteract() e successivamente ripulisce il set.

Il metodo `updateEntityPair(Entity entityA, Entity entityB, float deltaTime)`:
- Verifica che entrambe le entità possiedano MotionComponent e ColliderComponent, necessari per determinare la loro posizione e area di interazione.
- Calcola il centro delle bounding box delle due entità utilizzando calculateBoundingBoxCenter().
- Determina se le entità sono all’interno del raggio definito nel ProximityComponent:
   - Se lo sono, entityB viene aggiunta al set delle entità che attivano la prossimità.
   - Altrimenti viene rimossa.
- Se almeno un’entità attivante è presente, si imposta lo stato di prossimità (trigger.setTriggered(true)) e si gestisce un eventuale dialogo tramite handleDialogue().

Il metodo `handleDialogue(DialogueComponent dialogue, Entity entityA, Entity entityB)`:
- Si occupa di gestire l’avvio di dialoghi fra entità:
- Recupera il DialogueElement dal DialogueComponent.
- Controlla che l’entità attivante (entityB) abbia un KeyInputComponent.
- Verifica se l’azione “DIALOG” sia stata appena attivata dal giocatore.
- Se queste condizioni sono soddisfatte e il dialogo precedente è terminato, avvia un nuovo dialogo.

La classe interna Vector2:
-	Classe privata di utilità che rappresenta coordinate bidimensionali.
- Offre il metodo distanceSquared() per calcolare rapidamente la distanza quadrata fra due punti, utile per confronti rapidi evitando radici quadrate (ottimizzazione).

Punti di forza:
- Separazione delle responsabilità: Ogni metodo gestisce in modo chiaro un tipo specifico di interazione (diretta o di prossimità), mantenendo chiarezza e modularità.
- Ottimizzazione: Usa calcoli di distanza quadrata per efficienza.
- Gestione dinamica: Gestisce dinamicamente la prossimità e lo stato di attivazione delle interazioni.

Flessibilità del sistema:
- Altamente configurabile, grazie alla possibilità di definire logiche personalizzate tramite i componenti (InteractionComponent, DialogueComponent).
- Facilmente espandibile per integrare nuovi tipi di interazioni.

## 5. AudioManager

L'AudioManager è una classe centrale per la gestione dell'audio nel gioco, inclusi effetti sonori e musica di sottofondo. Questa classe permette di caricare, controllare e riprodurre file audio.

Le funzioni Principali dell'AudioManager sono:
- Caricamento Audio: Carica brani musicali e effetti sonori da file, creando per ciascuno un oggetto Clip che può essere controllato (riprodotto, fermato, ripetuto).
- Controllo del Volume: Modifica il volume di canzoni ed effetti sonori. Il volume è unificato per tutti gli effetti sonori e separatamente per le canzoni, permettendo una facile regolazione dell'audio di sottofondo rispetto agli effetti di gioco.
- Play e Stop: Fornisce metodi per riprodurre o fermare specifiche tracce audio e effetti sonori, incluso il supporto per la riproduzione in loop di brani di sottofondo.
- Mute/Unmute: Permette di attivare o disattivare l'audio delle canzoni o degli effetti sonori separatamente, utile per implementare funzioni di silenziamento audio nelle impostazioni del gioco.

I metodi a disposizione sono:
- `loadSongs` e `loadEffects`: Questi metodi caricano rispettivamente le tracce musicali e gli effetti sonori forniti come array di nomi di file, inizializzando gli array songs ed effects con le Clip corrispondenti.
- `getClip`: Questo metodo ausiliario carica un singolo file audio e lo prepara per la riproduzione, utilizzando `AudioSystem.getClip()` e apre lo stream di AudioInputStream per l'audio specificato.
- `setVolume`: aggiorna il volume globale e applica la modifica a tutti i brani ed effetti sonori. Utilizza `FloatControl.Type.MASTER_GAIN` per regolare il livello di gain delle Clip, calcolando il valore di gain basato sul volume desiderato.
- `playEffect`, `playSong`, e `loopSong`: gestiscono la riproduzione delle tracce, inclusa l'opzione di loop continuo per la musica di sottofondo.
- `toggleSongMute` e `toggleEffectMute`: gestiscono il silenziamento.

## 6. PlayerComponent (attributi giocatore)

PlayerComponent tiene traccia delle proprietà specifiche del giocatore, come il numero di gemme raccolte. Si possono aggiungere tutti gli attributi che servono: aggiungiamo il numero di gemme raccolte, per esempio:

```java
package engine.components;

public class PlayerComponent extends Component {
    private int gems;

    public PlayerComponent(Entity entity) {
        super(entity);
        this.gems = 0;
    }

    public int getGems() {
        return gems;
    }

    public void setGems(int gems) {
        this.gems = gems;
    }
}
```

## 7. Aggiunta degli elementi al metodo init() di PlayState

Per integrare correttamente le nuove funzionalità interattive e audio, seguiamo questi passaggi dettagliati nel metodo init() della classe PlayState.

1. **Inizializzazione AudioManager**: Per gestire gli effetti sonori e la musica del gioco, aggiungiamo alla classe PlayState l'attributo AudioManager audio.
```java
private AudioManager audio;
```
2. **Creazione dell'istanza di AudioManager**: inizializziamo poi l'attributo creato precedentemente all'inizio del metodo init.
```java
// Audio
String[] songNames = { "/music/main_theme.wav" };
String[] effectNames = { "/sfx/picked_gem.wav" };
audio = new AudioManager(songNames, effectNames);
audio.setVolume(1.0f);
```
3. **Aggiunta del PlayerComponent all'entity Player**: aggiungiamo all'entity Player il componente per salvare il numero di gemme.
```java
PlayerComponent playerComponent = new PlayerComponent(player);
player.addComponent(playerComponent);
```

4. **Creazione dell’entità raccoglibile (Gem)**: Dopo il player, aggiungiamo una nuova entità raccoglibile al gioco, in questo caso una “Gem”, con componenti appropriati per posizione, collisione, grafica e interazione.

```java
// Entity: Gem
Entity gem = new Entity(EntityType.ITEM, 2);
CharacterSpritesheet gemSpritesheet = new CharacterSpritesheet("/objects/gem.sprite");
MotionComponent gemPosition = new MotionComponent(gem, 1550, 1950, 0.0f);
gem.addComponent(gemPosition);

ColliderComponent gemCollider = new ColliderComponent(gem, gemSpritesheet.getBoundingBox(),
        CollisionBehavior.STATIC);
gem.addComponent(gemCollider);

SpriteComponent gemSprite = new SpriteComponent(gem, gemSpritesheet, .08f, true);
gem.addComponent(gemSprite);

// Componente di interazione
InteractionComponent interactableComponent = new InteractionComponent(gem);
interactableComponent.setOnEntityInteract(other -> {
    // Logica personalizzata quando il giocatore interagisce con l'entità
    playerComponent.addGems(1);    // Incrementa il contatore delle gemme raccolte
    gem.setAlive(false);           // Rimuove la gemma dal gioco
    audio.playEffect(0);           // Riproduce l'effetto sonoro di raccolta
});
gem.addComponent(interactableComponent);
add(gem);
```

5. **Inserimento dell’InteractionSystem**: Inseriamo nel gioco il sistema che gestisce dinamicamente le interazioni.
```java
add(new InteractionSystem(this.engine));
```

6. **Avvio musica di sottofondo**: Per rendere l’esperienza più coinvolgente, avviamo il tema musicale di sottofondo in loop alla fine del metodo init.

```java
audio.loopSong(0);
```

Sintesi delle modifiche apportate:
- Aggiunto un sistema audio (AudioManager) per effetti sonori e musica.
- Creata una nuova entità raccoglibile con logica di interazione specifica.
- Inserito InteractionSystem per gestire tutte le interazioni fra le entità e con l’ambiente di gioco.
- Implementata musica di sottofondo che accompagna il gameplay.
