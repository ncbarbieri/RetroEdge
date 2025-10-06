# Lezione 12 – Attacco del Player e del Nemico

Obiettivi della lezione
- Comprendere come gestire azioni offensive (attacco con spada o lancio di proiettili).
- Implementare i componenti e i relativi sistemi utili alla gestione dell'attacco.
- Gestire le collisioni durante un attacco e la riduzione della salute di un’entità.

## 1. HealthComponent – Gestione della salute delle entità

HealthComponent è il componente che si occupa di gestire i punti vita (HP) di un’entità di gioco, come il player o un nemico.
Ogni entità che può subire danni o essere distrutta deve avere un HealthComponent associato.

### 1.1 Scopo del componente

Questo componente:
- Tiene traccia dei punti vita correnti e massimi;
- Permette di aumentare o diminuire i punti vita;
- Segnala quando l’entità non è più in vita, grazie a una funzione lambda (onDeath) che possiamo personalizzare.

### 1.2 Codice del componente

```java
public class HealthComponent extends Component {

    private int health;
    private int maxHealth;
    private Runnable onDeath; // Callback eseguita alla morte

    public HealthComponent(Entity entity, int maxHealth) {
        super(entity);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void decreaseHealth(int amount) {
        health -= amount;
        if (health < 0) health = 0;

        // Se la salute arriva a zero, esegue la callback
        if (health == 0 && onDeath != null) {
            onDeath.run();
        }
    }

    public void increaseHealth(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    public void resetHealth() {
        health = maxHealth;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public boolean isDead() { return health <= 0; }

    public void setOnDeath(Runnable onDeath) {
        this.onDeath = onDeath;
    }

    public void kill() {
        health = 0;
        if (onDeath != null) onDeath.run();
    }
}
```

### 1.3 Spiegazione del funzionamento

| Metodo	| Descrizione |
|---------|-------------|
| decreaseHealth(int amount)	| Riduce i punti vita di una certa quantità. Se arriva a 0, richiama automaticamente la funzione onDeath. |
| increaseHealth(int amount)	| Aumenta i punti vita, ma non oltre il valore massimo. |
| resetHealth()	| Riporta la salute al valore massimo (utile per il respawn o il ripristino). |
| isDead()	| Ritorna true se la salute è uguale o minore di 0. |
| setOnDeath(Runnable onDeath)	| Permette di impostare una funzione da eseguire automaticamente alla morte dell’entità. |
| kill()	| Imposta direttamente i punti vita a 0 e richiama onDeath. |

### 1.4 Esempio di utilizzo (nel PlayState)

Nel caso del player, possiamo usare questo componente per gestire la perdita di vita e la “morte” del personaggio:

```java
// Creazione del componente salute del player
HealthComponent playerHealth = new HealthComponent(player, 5);

// Azione da eseguire quando il player muore
playerHealth.setOnDeath(() -> {
    System.out.println("Game Over!");
    audio.playEffect(4);         // Effetto sonoro
    player.setAlive(false);      // Disattiva l'entità
});

player.addComponent(playerHealth);
```

In questo esempio:
- Il player inizia con 5 punti vita;
- Ogni volta che viene colpito, il suo HealthComponent riduce i punti vita;
- Quando i punti vita raggiungono zero, viene automaticamente eseguita la lambda definita con setOnDeath.

Si può personalizzare la lambda onDeath per ogni tipo di entità:

| Entità	| Azione alla morte |
|---------|-------------------|
| Player	| Mostra la cutscene di “Game Over” e ferma il gioco. |
| Nemico	| Riproduce un'animazione di morte del nemico e incrementa il punteggio del player. |
| Boss	| Fa partire una cutscene o una sequenza di vittoria. |
| Oggetto distruttibile	| Emette particelle o fa comparire un bonus. |

## 2.  AttackComponent – Gestione dell’attacco

L’AttackComponent gestisce tutto ciò che riguarda la logica di un attacco corpo a corpo, cioè il comportamento di un colpo sferrato dal personaggio.
Questo componente non si occupa di disegnare l’attacco sullo schermo, ma di definire quando un attacco è attivo, quanto dura e quale zona dello spazio colpisce.

Quando un’entità (come il player o un nemico) preme il tasto d’attacco, entra in uno stato temporaneo in cui può colpire.
Durante questo periodo:
- viene attivata un’area chiamata hitbox, che rappresenta la zona d’effetto del colpo;
- se un’altra entità entra in contatto con questa hitbox, subisce danno;
- al termine della durata dell’attacco, la hitbox scompare e inizia un tempo di ricarica (cooldown) prima che si possa attaccare di nuovo.

In sintesi, il componente tiene traccia di:
- quanto dura l’attacco (duration);
- ogni quanto può essere ripetuto (cooldown);
- quanto danno infligge (damage);
- le diverse hitbox associate a ciascuna direzione di attacco (hitBoxes).

### 2.1  Struttura del componente

```java
public class AttackComponent extends Component {

    private float duration;       // Durata dell'attacco
    private float cooldown;       // Tempo minimo tra attacchi
    private float damage;         // Danno inflitto
    private boolean attacking;    // Se l'entità sta attaccando
    private boolean canAttack;    // Se è pronta ad attaccare
    private float timer;          // Cronometro interno
    private Runnable onAttack;    // Azione da eseguire all'attacco
    private Map<Direction, Rectangle> hitBoxes; // Hitbox per direzione
}
```

Spiegazione dei campi principali

| Variabile	| Descrizione |
|-----------|-------------|
| duration	| Indica quanto tempo dura l’attacco. Durante questo intervallo l’entità può colpire i nemici. |
| cooldown	| È il tempo minimo di attesa tra un attacco e quello successivo. Serve per evitare che il personaggio attacchi continuamente senza pausa. |
| damage	| Indica quanti punti vita vengono tolti al bersaglio quando l’attacco va a segno. |
| attacking	| Vale true quando l’attacco è in corso, cioè durante la finestra temporale in cui l’hitbox è attiva. |
| canAttack	| Vale true se l’entità è pronta ad attaccare. Diventa false durante il cooldown. |
| timer	| È un cronometro interno che misura sia la durata dell’attacco che il tempo di cooldown. |
| onAttack	| È una funzione lambda eseguita nel momento esatto in cui parte l’attacco, utile per effetti sonori o animazioni. |
| hitBoxes	| È una mappa che associa a ogni direzione (alto, basso, sinistra, destra) una hitbox, cioè un rettangolo che definisce la zona colpita. |

### 2.2 Che cos’è una hitbox

Nel contesto dei videogiochi, una hitbox è un’area invisibile che rappresenta la zona di impatto di un colpo o di un personaggio.
Quando due hitbox si sovrappongono, il motore rileva una collisione, e l’evento corrispondente (come la perdita di vita) viene attivato.

Nel caso dell’attacco del player, per ogni direzione di movimento viene definito un rettangolo (Rectangle) che rappresenta l’area in cui la spada colpisce.
Esempio:

| Direzione	| Hitbox (x, y, width, height)	| Descrizione |
|-----------|-------------------------------|-------------|
| Destra	| (96, 68, 40, 16)	| La spada colpisce di lato, davanti al personaggio. |
| Sinistra	| (8, 68, 40, 16)	| Colpo verso sinistra. |
| Alto	| (56, 0, 16, 40)	| Colpo sopra la testa. |
| Basso	| (72, 92, 16, 40)	| Colpo verso il basso. |

Durante l’attacco, l’AttackSystem prenderà la hitbox relativa alla direzione corrente e verificherà se entra in contatto con un’altra entità dotata di HealthComponent.

### 2.3 Principali metodi

| Metodo	| Descrizione |
|-----------|-------------|
| startAttack()	| Avvia un attacco se disponibile (controlla che canAttack sia vero e che non ci sia un cooldown in corso). Attiva la hitbox e la lambda onAttack. |
| update(float deltaTime)	| Aggiorna il timer interno per misurare durata e cooldown dell’attacco. Disattiva la hitbox al termine della durata e riabilita l’attacco dopo il cooldown. |
| setHitBox(Direction, Rectangle)	| Imposta la zona colpita per una determinata direzione. |
| getHitBox(Direction)	| Restituisce la hitbox corrispondente alla direzione corrente. |
| setOnAttack(Runnable)	| Imposta un effetto da eseguire nel momento in cui inizia l’attacco (per esempio un suono o una scia luminosa). |

## 3. AttackSystem – Applicazione del danno

L’AttackSystem è il sistema che trasforma l’attacco in un’azione concreta di gioco.
Durante ogni aggiornamento del motore, controlla se un’entità sta eseguendo un attacco, calcola la posizione dell’hitbox attiva e verifica se questa colpisce altre entità.
Se una collisione viene rilevata, viene applicato un danno alla salute del bersaglio tramite il suo HealthComponent.

### 3.1 Struttura del sistema

```java
public class AttackSystem extends System {

    public AttackSystem(Engine engine) {
        super(engine);
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> entities = engine.getEntitiesWith(AttackComponent.class, ColliderComponent.class);

        for (Entity attacker : entities) {
            AttackComponent attack = attacker.getComponent(AttackComponent.class);
            ColliderComponent collider = attacker.getComponent(ColliderComponent.class);

            attack.update(deltaTime);

            if (!attack.isAttacking()) continue;

            Rectangle attackBox = attack.getHitBox(collider.getDirection());
            if (attackBox == null) continue;

            Rectangle worldHitBox = new Rectangle(
                collider.getBounds().x + attackBox.x,
                collider.getBounds().y + attackBox.y,
                attackBox.width,
                attackBox.height
            );

            for (Entity target : engine.getEntitiesWith(ColliderComponent.class, HealthComponent.class)) {
                if (target == attacker) continue;

                ColliderComponent targetCollider = target.getComponent(ColliderComponent.class);
                HealthComponent targetHealth = target.getComponent(HealthComponent.class);

                if (CollisionUtils.checkCollision(worldHitBox, targetCollider.getBounds())) {
                    targetHealth.decreaseHealth((int) attack.getDamage());
                }
            }
        }
    }
}
```

L’AttackSystem lavora a stretto contatto con il AttackComponent e il ColliderComponent:
1.	Recupera tutte le entità che possono eseguire un attacco (cioè quelle che possiedono sia un AttackComponent sia un ColliderComponent).
2.	Aggiorna lo stato di ogni attacco (durata e cooldown).
3.	Se l’attacco è in corso (isAttacking() restituisce true):
    - Calcola la hitbox effettiva nel mondo di gioco, sommando la posizione del personaggio alla hitbox locale;
    - Confronta questa area con la posizione di tutte le entità che possiedono un HealthComponent;
    - Se le due aree si sovrappongono, l’entità bersaglio subisce danno.

### 3.2 Descrizione passo per passo

1. Selezione delle entità attaccanti

```java
List<Entity> entities = engine.getEntitiesWith(AttackComponent.class, ColliderComponent.class);
```

Il sistema cerca tutte le entità che possono attaccare, cioè quelle che hanno sia un attacco che un collider (per conoscere la loro posizione e direzione).

2. Aggiornamento dello stato dell’attacco

```java
attack.update(deltaTime);
if (!attack.isAttacking()) continue;
```

Ogni attacco viene aggiornato in base al tempo trascorso (deltaTime):
- Se l’attacco non è attivo, il ciclo passa all’entità successiva.
- Se invece è in corso, il sistema calcola la hitbox corrispondente alla direzione attuale.

3. Calcolo della hitbox globale

```java
Rectangle attackBox = attack.getHitBox(collider.getDirection());
Rectangle worldHitBox = new Rectangle(
    collider.getBounds().x + attackBox.x,
    collider.getBounds().y + attackBox.y,
    attackBox.width,
    attackBox.height
);
```

Ogni hitbox definita nel AttackComponent è relativa al personaggio, cioè parte dal suo punto di origine.
Per verificare le collisioni nel mondo di gioco, è necessario traslare la hitbox locale in una posizione assoluta, aggiungendo le coordinate del personaggio.

4. Verifica delle collisioni

```java
for (Entity target : engine.getEntitiesWith(ColliderComponent.class, HealthComponent.class)) {
    if (target == attacker) continue;
    ...
    if (CollisionUtils.checkCollision(worldHitBox, targetCollider.getBounds())) {
        targetHealth.decreaseHealth((int) attack.getDamage());
    }
}
```

Il sistema:
- controlla tutte le entità che possono essere colpite, cioè quelle che hanno un HealthComponent;
- evita di colpire sé stessa (if (target == attacker) continue);
- usa la funzione CollisionUtils.checkCollision() per verificare la sovrapposizione tra le due aree;
- se l’intersezione è confermata, applica il danno riducendo la salute del bersaglio.

### 3.3 Il ruolo della direzione

AttackSystem non calcola direttamente la direzione del personaggio:
questa informazione è fornita dal ColliderComponent, che tiene traccia della direzione di movimento o di orientamento (Direction.UP, DOWN, LEFT, RIGHT).

L’attacco quindi usa:
- la direzione per scegliere l’hitbox giusta dall'AttackComponent;
- la posizione del collider per determinare dove si trova realmente nel mondo.

Esempio pratico

Quando il giocatore preme SPAZIO:
1.	InputSystem chiama startAttack() → l’attacco parte.
2.	L’AttackComponent attiva la hitbox per un breve periodo (duration).
3.	L’AttackSystem calcola la posizione reale della hitbox e verifica se colpisce un nemico.
4.	Se sì, il HealthComponent del nemico riduce i punti vita.
5.	L’attacco termina e inizia il cooldown prima di poter colpire di nuovo.

### 3.4 Collaborazione tra componenti e sistemi

| Elemento	| Ruolo |
|-----------|-------|
| AttackComponent	| Gestisce durata, cooldown e danno dell’attacco. |
| ColliderComponent	| Fornisce posizione e direzione per calcolare la hitbox globale. |
| HealthComponent	| Subisce il danno se la hitbox lo colpisce. |
| AttackSystem	| Coordina il tutto e applica il danno effettivo. |

