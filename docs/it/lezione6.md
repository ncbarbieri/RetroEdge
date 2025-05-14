# Lezione 6: Interazione con gli oggetti e gestione dell'audio

**Obiettivi della lezione**:
- Implementare meccaniche di interazione nel mondo di gioco.
- Creare oggetti raccoglibili generici.
- Gestire attributi specifici del giocatore.

## 1. Introduzione all’interazione con gli oggetti

Un gioco diventa più coinvolgente quando offre possibilità di interazione con oggetti o personaggi. In questa lezione impareremo a introdurre meccaniche interattive, come la raccolta di oggetti utili.

Le classi coinvolte sono:
- InteractionComponent
- InteractionSystem

## 2. InteractionComponent

La classe InteractionComponent permette alle entità di reagire dinamicamente alle interazioni nel mondo di gioco, sia con altre entità che con specifiche zone o tile della mappa. È progettata per fornire una grande flessibilità nel definire logiche differenti per ciascun tipo di interazione.

Attributi:

InteractionComponent utilizza principalmente le seguenti strutture dati e tipi funzionali:
- `Boolean interactable`: Indica se l’entity può interagire o meno. Permette di abilitare o disabilitare le interazioni dinamicamente.
- `Consumer onEntityInteract`: È una funzione che accetta come parametro un oggetto di tipo Entity. Quando un’entità entra in interazione con un’altra, questo metodo definisce la logica da eseguire.
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

## 3. InteractionSystem

InteractionSystem gestisce in modo centralizzato tutte le interazioni che avvengono tra le entità nel gioco, occupandosi sia di interazioni dirette (entity-entity) sia delle interazioni tra entità e tiles della mappa di gioco. Questo sistema controlla e risponde dinamicamente agli eventi di interazione e prossimità, avviando dialoghi, raccogliendo oggetti o attivando meccanismi di gioco.

La logica di interazione è suddivisa in due fasi principali:

1. Elaborazione delle interazioni:
    - Cicla su tutte le entità con un InteractionComponent.
    - Chiama il metodo `handleInteraction()` per ciascuna entità, gestendo le interazioni accumulate durante il ciclo.
2. Gestione delle interazioni basate su prossimità:
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


5. PlayerComponent (attributi giocatore)

PlayerComponent tiene traccia delle proprietà specifiche del giocatore, come il numero di gemme raccolte.

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


⸻

6. Implementazione in PlayState (aggiornata)

Aggiungiamo il componente giocatore al personaggio principale:

PlayerComponent playerComponent = new PlayerComponent(link);
link.addComponent(playerComponent);

Creiamo quindi l’oggetto interattivo generico (Gem):

// Entity: Gem
Entity gem = new Entity(2, EntityType.ITEM);
PositionComponent gemPosition = new PositionComponent(gem, 1550, 1850);
gem.addComponent(gemPosition);

Gem gemSpritesheet = new Gem();
ColliderComponent gemCollider = new ColliderComponent(gem, gemSpritesheet.getBoundingBox());
gem.addComponent(gemCollider);

RenderComponent gemRender = new RenderComponent(gem);
gem.addComponent(gemRender);

SpriteComponent gemSprite = new SpriteComponent(gem, gemSpritesheet, 5, true);
gem.addComponent(gemSprite);

InteractionComponent interactionComponent = new InteractionComponent(gem);
interactionComponent.setOnInteract(() -> {
    // Incrementa il numero di Gemme e distrugge l’oggetto
    playerComponent.setGems(playerComponent.getGems() + 1);
    gem.setAlive(false);
});
gem.addComponent(interactionComponent);

add(gem);

Aggiungiamo il sistema di interazione:

add(new InteractionSystem(this.engine, link));


⸻

Conclusione (aggiornata)

Usando un termine generico come “Gem”, abbiamo mantenuto la stessa struttura modulare del gioco, eliminando riferimenti specifici e offrendo una soluzione facilmente riutilizzabile in qualsiasi contesto.

⸻

Se ti serve modificare altro, fammi sapere!
