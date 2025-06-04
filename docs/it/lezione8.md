# Lezione 8 – NPC con dialoghi interattivi

In questa lezione aggiungeremo al nostro gioco un personaggio non giocante (NPC - non-player character) con le seguenti funzionalità:
- movimento automatico tra due posizioni
- rilevamento della prossimità del giocatore
- notifica visiva quando ci si avvicina
- interazione tramite dialogo


## 1. NPCComponent

Nel nostro motore di gioco, il movimento delle entità è gestito tramite due elementi principali:
1. MotionComponent: tiene traccia della posizione e della velocità di un’entità.
2. MotionSystem: aggiorna la posizione delle entità in base al tempo trascorso (delta time) e alla loro velocità.

Mentre il player viene mosso grazie a KeyInputComponent, abbiamo bisogno di un nuovo componente per guidare gli spostamenti di un NPC. Introduciamo quindi NPCComponent, un componente che permette di assegnare a un NPC una destinazione, calcolare la direzione da seguire e aggiornare la velocità di conseguenza.

Il NPCComponent estende il comportamento del MotionComponent con l’aggiunta di:
- coordinate di destinazione (targetX, targetY)
- funzione setTargetPosition per cambiare destinazione
- funzione updateVelocity per calcolare la direzione del movimento
- callback onTargetReached per definire un comportamento al raggiungimento della destinazione

Quando la distanza residua è piccola (sotto una soglia), l’NPC si ferma e viene eseguito il codice associato al callback.

La classe MotionSystem scorre tutte le entità dotate di MotionComponent e aggiorna la loro posizione nel mondo. Se l’entità ha un componente NPCComponent, la velocità viene calcolata in base alla direzione verso la destinazione. Quando l’NPC raggiunge la destinazione, viene notificato il callback definito tramite setOnTargetReached. Con queste due classi, il sistema è in grado di gestire NPC che si spostano autonomamente tra più punti, aggiornando la direzione e la velocità in tempo reale, e reagendo al raggiungimento di ciascun obiettivo.

## 2. ProximityComponent: rilevamento della vicinanza

Il ProximityComponent consente a un’entità (tipicamente un NPC) di rilevare la vicinanza di altre entità (es. il giocatore) entro un determinato raggio di interazione. Quando la vicinanza viene rilevata, può:
- attivare uno stato interno
- visualizzare un elemento grafico di notifica (es. un fumetto)

Attributi principali:

| Campo	| Tipo	| Descrizione |
|-------|-------|-------------|
| interactionRange	| float	| Raggio entro cui avviene l’interazione. |
| activationFilter	| Predicate<Entity>	| Funzione che definisce chi può attivare la prossimità. |
| isTriggered	| boolean	| Stato corrente: true se almeno una entità è vicina. |
| triggeringEntities	| Set<Entity>	| Insieme delle entità che stanno attivando la prossimità. |
| notificationElement	| UINotification	| Icona grafica associata (es. fumetto). |


### 2.1 Costruttori

```java
public ProximityComponent(Entity entity, float interactionRange)
```

Inizializza con un range e nessun filtro (tutte le entità possono attivarlo).

```java
public ProximityComponent(Entity entity, float interactionRange, Predicate<Entity> activationFilter)
```

Aggiunge anche un filtro che può limitare chi ha il diritto di attivare il componente (es. solo il player).

### 2.2 Metodi principali

isTriggered()

Restituisce lo stato attuale (true se almeno un’entità ha attivato la prossimità).

addTriggeringEntity(Entity entity)

Aggiunge un’entità tra quelle che stanno attivando il componente. Se supera il filtro (canActivate()), attiva lo stato e mostra la notifica (se presente).

removeTriggeringEntity(Entity entity)

Rimuove un’entità dal set. Se non rimane più nessuna entità attiva, disattiva il componente e nasconde la notifica.

clearTriggeringEntities()

Rimuove tutte le entità e resetta lo stato a false.

canActivate(Entity other)

Valuta se una certa entità può attivare la prossimità secondo il filtro. Se non è definito alcun filtro (null), qualsiasi entità è valida.
