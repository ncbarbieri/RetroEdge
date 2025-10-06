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
List<Entity> entities = engine.getEntitiesWith(AttackComponent.class);
```

Il sistema cerca tutte le entità che possono attaccare, cioè quelle che hanno il componente per l'attacco.

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
questa informazione è fornita dallo SpriteComponent, che tiene traccia della direzione di movimento o di orientamento (Direction.UP, DOWN, LEFT, RIGHT).

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

## 4. TimerComponent e TimerSystem – Gestione del tempo e degli eventi periodici

Molte azioni nei videogiochi devono avvenire dopo un certo intervallo di tempo o ripetersi ciclicamente:
un nemico che attacca ogni 5 secondi, una trappola che si attiva a intervalli regolari, oppure un effetto che dura solo per un tempo limitato.

Nel motore Retro Edge, questo comportamento è gestito da due elementi:
- il TimerComponent, che tiene traccia del tempo trascorso per una determinata entità;
- il TimerSystem, che aggiorna tutti i timer e attiva gli eventi associati quando scadono.

### 4.1  TimerComponent – Gestione del tempo di attesa

Il TimerComponent rappresenta un conto alla rovescia legato a una singola entità.
Può essere usato per eseguire un’azione automatica dopo un certo intervallo (ad esempio, far attaccare un nemico o far esplodere una bomba).

### 4.2 Struttura del componente

```java
package engine.components;

import engine.Component;
import engine.Entity;

public class TimerComponent extends Component {

    private float time;                 // Durata totale del timer (in secondi)
    private float elapsedTime;          // Tempo trascorso
    private boolean loop;               // True se il timer si ripete ciclicamente
    private boolean active;             // True se il timer è in esecuzione
    private Runnable onTimeOver;        // Azione da eseguire allo scadere del timer

    public TimerComponent(Entity entity, float time, boolean loop) {
        super(entity);
        this.time = time;
        this.loop = loop;
        this.active = true;
        this.elapsedTime = 0;
    }

    public void update(float deltaTime) {
        if (!active) return;

        elapsedTime += deltaTime;
        if (elapsedTime >= time) {
            if (onTimeOver != null) onTimeOver.run();
            if (loop) elapsedTime = 0;
            else active = false;
        }
    }

    public void reset() {
        elapsedTime = 0;
        active = true;
    }

    public void stop() {
        active = false;
    }

    public void start() {
        active = true;
    }

    public void setOnTimeOver(Runnable onTimeOver) {
        this.onTimeOver = onTimeOver;
    }

    public boolean isActive() {
        return active;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public float getTime() {
        return time;
    }
}
```

Il componente TimerComponent tiene traccia del tempo trascorso per una specifica entità.
Una volta raggiunta la durata impostata (time), il timer può:
- eseguire un’azione (tramite una funzione lambda onTimeOver);
- fermarsi, oppure ricominciare automaticamente se impostato come looping.

### 4.3 Campi principali

| Variabile	| Descrizione |
|-----------|-------------|
| time	| Durata totale del timer in secondi. |
| elapsedTime	| Tempo trascorso dall’avvio del timer. |
| loop	| Se true, il timer si ripete automaticamente. |
| active	| Indica se il timer è attualmente in esecuzione. |
| onTimeOver	| Funzione eseguita automaticamente allo scadere del timer. |

### 4.4 Metodi principali

| Metodo	| Descrizione |
|-----------|-------------|
| update(deltaTime)	| Aggiorna il tempo trascorso. Se il timer scade, esegue onTimeOver. |
| reset()	| Riporta il timer a zero e lo riattiva. |
| stop() / start()	| Ferma o riavvia il conteggio. |
| setOnTimeOver(Runnable)	| Imposta l’azione da eseguire quando il timer termina. |

### 4.5 Funzionamento

Durante ogni aggiornamento del gioco, il sistema incrementa il valore elapsedTime con il tempo trascorso (deltaTime).
Quando elapsedTime raggiunge la soglia time, viene eseguita la callback onTimeOver().
A seconda della configurazione:
- se loop = false, il timer si disattiva;
- se loop = true, il timer riparte da zero automaticamente.

Questo meccanismo permette di gestire eventi temporizzati in modo completamente modulare.

### 4.6  TimerSystem – Aggiornamento globale dei timer

Il TimerSystem è il sistema che controlla e aggiorna tutti i timer del gioco.
Ogni entità può avere uno o più timer indipendenti, e il sistema garantisce che tutti vengano aggiornati in sincronia con il tempo di gioco.

### 4.7 Struttura del sistema

```java
package engine.systems;

import java.util.List;
import engine.Engine;
import engine.Entity;
import engine.System;
import engine.components.TimerComponent;

public class TimerSystem extends System {

    public TimerSystem(Engine engine) {
        super(engine);
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> entities = engine.getEntitiesWith(TimerComponent.class);

        for (Entity entity : entities) {
            TimerComponent timer = entity.getComponent(TimerComponent.class);
            timer.update(deltaTime);
        }
    }
}
```

Il TimerSystem scorre tutte le entità che possiedono un TimerComponent e ne aggiorna lo stato chiamando il metodo update(deltaTime) per ciascuna di esse.
Questo consente di sincronizzare i timer con il ciclo di aggiornamento del motore.

In altre parole:
- ogni entità può definire il proprio tempo di azione;
- il sistema tiene conto del tempo trascorso tra un frame e l’altro;
- quando un timer scade, esegue automaticamente l’azione definita in onTimeOver.

Il TimerComponent può essere utilizzato per molte funzioni diverse:
- far attaccare un nemico a intervalli regolari;
- gestire effetti temporanei, come un potenziamento o un effetto grafico;
- controllare sequenze di gioco o animazioni (ad esempio l’apertura automatica di una porta);
- far scadere il tempo di un livello o di una prova.

Grazie alla sua semplicità e flessibilità, questo componente è una delle basi per la gestione degli eventi temporali nel motore.

### 4.8  Collaborazione con altri sistemi

Il TimerComponent è spesso utilizzato insieme ad altri componenti per creare comportamenti complessi.
Ecco alcuni esempi tipici:

| Combinazione	| Effetto ottenuto |
|---------------|------------------|
| TimerComponent + ThrowProjectileComponent	| Il nemico lancia un proiettile ogni N secondi. |
| TimerComponent + AttackComponent	| Il nemico esegue un attacco corpo a corpo periodico. |
| TimerComponent + SpriteComponent	| Cambia l’animazione dell’entità dopo un certo tempo. |
| TimerComponent + HealthComponent	| Riduce gradualmente la vita (danno nel tempo). |

### 4.9 Vantaggi del sistema di timer

| Vantaggio	| Descrizione |
|-----------|-------------|
| Disaccoppiamento	| L’entità non deve sapere come viene gestito il tempo, ma solo quando vuole che qualcosa accada. |
| Riutilizzabilità	| Lo stesso timer può essere usato per qualsiasi tipo di evento. |
| Controllo centralizzato	| Tutti i timer vengono aggiornati da un unico sistema, mantenendo la coerenza temporale. |
| Modularità	| È possibile aggiungere o rimuovere timer in modo indipendente dalle altre logiche di gioco. |

### 4.10  Sintesi

| Elemento	| Responsabilità |
|-----------|----------------|
| TimerComponent	| Gestisce il tempo e le azioni da eseguire allo scadere. |
| TimerSystem	| Aggiorna tutti i timer del gioco, garantendo la sincronizzazione temporale. |
| onTimeOver()	| Permette di definire il comportamento personalizzato al termine del timer. |

## 5.  Attacchi a distanza – ThrowProjectileComponent e ProjectileSystem

Dopo aver introdotto gli attacchi corpo a corpo, questa lezione affronta il tema degli attacchi a distanza, una meccanica comune nei giochi d’azione e di ruolo.
Nel nostro motore, questa funzione è gestita da due elementi principali:
- un componente di lancio (ThrowProjectileComponent), che si occupa della preparazione del colpo e della creazione del proiettile;
- un sistema di gestione dei proiettili (ProjectileSystem), che ne controlla il movimento, la durata e le collisioni.

### 5.1  ThrowProjectileComponent – Logica del lancio

Il ThrowProjectileComponent definisce la logica del lancio di un proiettile.
Non gestisce direttamente il movimento, ma stabilisce:
- quando deve essere lanciato un proiettile,
- quale proiettile utilizzare,
- con che velocità e per quanto tempo rimarrà attivo nel mondo.

### 5.2 Struttura del componente

```java
package engine.components;

import engine.Component;
import engine.Entity;

public class ThrowProjectileComponent extends Component {

    private float lifetime;        // Durata di vita del proiettile
    private float speed;           // Velocità del proiettile
    private Entity projectile;     // Entità del proiettile da lanciare
    private boolean throwing;      // True se è stato richiesto un lancio

    public ThrowProjectileComponent(Entity entity, float lifetime, float speed, Entity projectile) {
        super(entity);
        this.lifetime = lifetime;
        this.speed = speed;
        this.projectile = projectile;
        this.throwing = false;
    }

    public void throwProjectile() {
        throwing = true; // Segnala al sistema che il proiettile deve essere lanciato
    }

    public boolean isThrowing() {
        return throwing;
    }

    public void setThrowing(boolean throwing) {
        this.throwing = throwing;
    }

    public float getLifetime() {
        return lifetime;
    }

    public float getSpeed() {
        return speed;
    }

    public Entity getProjectile() {
        return projectile;
    }
}
```

Il ThrowProjectileComponent serve a preparare un lancio.
Ogni entità che può lanciare un proiettile (come un arciere o un cavaliere nemico) possiede questo componente.
Quando il suo stato interno (throwing) passa a true, il ProjectileSystem si occuperà di:
- attivare il proiettile associato;
- impostarne la direzione e la velocità;
- posizionarlo nel mondo di gioco.

Campi principali

| Variabile	| Descrizione |
|-----------|-------------|
| lifetime	| Indica per quanto tempo il proiettile rimane attivo prima di essere distrutto. |
| speed	| Rappresenta la velocità con cui si muove il proiettile dopo il lancio. |
| projectile	| È l’entità che rappresenta il proiettile (freccia, lancia, magia, ecc.). |
| throwing	| È un flag booleano che segnala al sistema che è stato richiesto un lancio. |

### 5.3 Funzionamento generale

Quando un’entità decide di attaccare a distanza (per esempio perché è scaduto un timer o perché il giocatore è a portata visiva), viene chiamato il metodo throwProjectile().
Da quel momento:
1.	il componente imposta throwing = true;
2.	il ProjectileSystem intercetta questo stato e si occupa di eseguire il lancio effettivo;
3.	una volta attivato il proiettile, throwing torna a false.

Questo meccanismo disaccoppia completamente la decisione di lanciare dal lancio effettivo, rispettando il principio di separazione delle responsabilità tipico del modello ECS.

### 5.4 ProjectileSystem – Gestione dei proiettili

Il ProjectileSystem è il sistema che controlla tutti i proiettili presenti nel mondo di gioco.
Il suo compito è duplice:
- creare e lanciare nuovi proiettili quando viene richiesto;
- aggiornare la posizione dei proiettili esistenti e disattivarli quando necessario.

### 5.5 Struttura del sistema

```java
package engine.systems;

import java.util.List;
import engine.Engine;
import engine.Entity;
import engine.System;
import engine.components.ThrowProjectileComponent;
import engine.components.MotionComponent;
import engine.components.ColliderComponent;
import engine.enums.Direction;

public class ProjectileSystem extends System {

    public ProjectileSystem(Engine engine) {
        super(engine);
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> throwers = engine.getEntitiesWith(ThrowProjectileComponent.class, ColliderComponent.class);

        for (Entity thrower : throwers) {
            ThrowProjectileComponent throwComp = thrower.getComponent(ThrowProjectileComponent.class);
            ColliderComponent throwerCollider = thrower.getComponent(ColliderComponent.class);

            // Se è in corso un lancio
            if (throwComp.isThrowing()) {
                Entity projectile = throwComp.getProjectile();
                ColliderComponent projectileCollider = projectile.getComponent(ColliderComponent.class);
                MotionComponent projectileMotion = projectile.getComponent(MotionComponent.class);

                // Posizione iniziale del proiettile
                projectileCollider.getBounds().x = throwerCollider.getBounds().x;
                projectileCollider.getBounds().y = throwerCollider.getBounds().y;
                projectile.setAlive(true);

                // Direzione e velocità del proiettile
                Direction direction = throwerCollider.getDirection();
                projectileMotion.setDirection(direction);
                projectileMotion.setSpeed(throwComp.getSpeed());

                // Disattiva il flag di lancio
                throwComp.setThrowing(false);
            }
        }

        // Aggiornamento dei proiettili attivi
        for (Entity projectile : engine.getEntitiesWith(MotionComponent.class)) {
            if (!projectile.isAlive()) continue;

            MotionComponent motion = projectile.getComponent(MotionComponent.class);
            motion.updatePosition(deltaTime);
        }
    }
}
```

### 5.6 Spiegazione generale

Il ProjectileSystem è responsabile dell’intero ciclo di vita dei proiettili, dalla creazione fino alla disattivazione.
Si basa sullo stato dei componenti per capire quando un proiettile deve essere lanciato e come deve muoversi.

Funzioni principali:

| Funzione	| Descrizione |
|-----------|-------------|
| Controllo dei lanciatori	| Il sistema individua tutte le entità che possiedono un ThrowProjectileComponent e un ColliderComponent. |
| Lancio del proiettile	| Se il flag throwing è attivo, il sistema posiziona e attiva il proiettile, impostandone direzione e velocità. |
| Aggiornamento del movimento	| Tutti i proiettili attivi vengono spostati nel mondo in base ai dati del MotionComponent. |
| Disattivazione	| Quando un proiettile esce dallo schermo o collide con un’entità, un altro componente (InteractionComponent) ne gestisce la disattivazione. |

### 5.7 Collaborazione tra componenti

Il ProjectileSystem non agisce da solo: funziona in coordinamento con altri componenti del motore di gioco, secondo la logica del modello ECS.

| Componente	| Ruolo |
|---------------|-------|
| ThrowProjectileComponent	| Richiede il lancio del proiettile. |
| MotionComponent	| Gestisce la velocità e l’aggiornamento della posizione del proiettile. |
| ColliderComponent	e MotionComponent | Forniscono la posizione iniziale di lancio del proiettile a seconda della posizione iniziale del lanciatore. |
| InteractionComponent	| Rileva le collisioni con tile o entità. |
| HealthComponent	| Gestisce la riduzione della vita in caso di impatto. |

### 5.8  Flusso generale dell’attacco a distanza

Il comportamento complessivo di un attacco a distanza può essere riassunto nelle seguenti fasi:
1.	Preparazione del lancio – un componente o un’IA decide che è il momento di attaccare.
2.	Segnalazione del lancio – il flag throwing del ThrowProjectileComponent viene impostato a true.
3.	Attivazione del proiettile – il ProjectileSystem rileva la richiesta e posiziona il proiettile a seconda della direzione del lanciatore.
4.	Movimento – il MotionComponent sposta il proiettile frame dopo frame.
5.	Collisione – se il proiettile incontra un ostacolo o un’entità, l’InteractionComponent gestisce la risposta (danno, distruzione, ecc.).
6.	Fine vita – il proiettile viene disattivato quando ha terminato la sua traiettoria o la sua durata.


### 5.9  Vantaggi dell’approccio ECS

L’introduzione dei proiettili nel motore mantiene la coerenza con il modello Entity–Component–System.
Ogni parte del comportamento è isolata e indipendente:

| Vantaggio	| Descrizione |
|-----------|-------------|
| Modularità	| Nuovi tipi di proiettili possono essere aggiunti senza modificare i sistemi esistenti. |
| Riutilizzabilità	| Lo stesso sistema funziona per nemici, boss o player. |
| Semplicità di test	| Ogni componente può essere verificato singolarmente. |
| Flessibilità	| È possibile definire effetti diversi all’impatto (danno, esplosione, rimbalzo, ecc.). |

### 5.10  Sintesi

| Elemento	| Responsabilità |
|-----------|----------------|
| ThrowProjectileComponent	| Definisce la logica di lancio e i parametri del proiettile. |
| ProjectileSystem	| Gestisce la creazione, il movimento e la durata dei proiettili. |
| MotionComponent	| Aggiorna la posizione nel tempo del proiettile. |
| InteractionComponent	| Gestisce gli impatti con tile e altre entità. |
| HealthComponent	| Riduce la vita dei bersagli colpiti. |

## 6. Integrazione nel PlayState – Attacco del player e del nemico

In questa sezione concludiamo la lezione sugli attacchi, mostrando come integrare nel PlayState tutti i componenti e i sistemi introdotti:
- l’attacco del player con la spada;
- il lancio di proiettili da parte del nemico;
- la gestione dei timer e degli eventi periodici.

### 6.1  Aggiunta dei componenti al Player

Il player deve poter:
1.	avere un certo numero di punti vita;
2.	eseguire un attacco corpo a corpo;
3.	attivare l’attacco tramite un tasto (in questo caso Spazio).

```java
// Da aggiungere all'entity player
HealthComponent playerHealth = new HealthComponent(player, 5);
player.addComponent(playerHealth);

AttackComponent playerAttacker = new AttackComponent(player, 0.25f, 0.15f, 1);
playerAttacker.setHitBox(Direction.RIGHT, new Rectangle(96, 68, 40, 16));
playerAttacker.setHitBox(Direction.UP, new Rectangle(56, 0, 16, 40));
playerAttacker.setHitBox(Direction.LEFT, new Rectangle(8, 68, 40, 16));
playerAttacker.setHitBox(Direction.DOWN, new Rectangle(72, 92, 16, 40));

playerAttacker.setOnAttack(() -> {
    audio.playEffect(2); // Effetto sonoro del colpo
});

player.addComponent(playerAttacker);
```

Spiegazione:
- HealthComponent gestisce i punti vita del player;
- AttackComponent definisce durata, cooldown e danno dell’attacco corpo a corpo;
- ogni direzione di attacco ha la propria hitbox;
- setOnAttack() riproduce un suono quando il colpo viene sferrato.

### 6.2  Creazione dell’entità proiettile (Spear)

Per il nemico che attacca a distanza, definiamo un’entità separata che rappresenta il proiettile.
Il proiettile viene attivato e disattivato dal sistema in base alle collisioni.

```java
// La freccia che viene lanciata:
// Entity: Spear
Entity spear = new Entity(EntityType.PROJECTILE, 2);
spear.setAlive(false);

MotionComponent spearPosition = new MotionComponent(spear, 0, 0, 400f);
spear.addComponent(spearPosition);

CharacterSpritesheet spearSpritesheet = new CharacterSpritesheet("/enemies/spear.sprite");
ColliderComponent spearCollider = new ColliderComponent(spear, spearSpritesheet, CollisionBehavior.DYNAMIC);
spear.addComponent(spearCollider);

SpriteComponent spearSprites = new SpriteComponent(spear, spearSpritesheet, .2f, true);
spear.addComponent(spearSprites);

CollisionMapComponent spearCollisionMap = new CollisionMapComponent(spear, world);
spear.addComponent(spearCollisionMap);

InteractionComponent spearInteraction = new InteractionComponent(spear);
spearInteraction.setOnTileInteract((Set<Node> nodes) -> {
    spear.setAlive(false); // si distrugge all’impatto con una tile
});

spearInteraction.setOnEntityInteract((Entity entity) -> {
    if (entity.equals(player)) {
        playerHealth.decreaseHealth(1); // il player subisce danno
    }
    spear.setAlive(false);
});

spear.addComponent(spearInteraction);
```

Spiegazione:
- MotionComponent definisce la velocità di movimento del proiettile;
- ColliderComponent e CollisionMapComponent permettono la rilevazione delle collisioni;
- InteractionComponent gestisce il comportamento al contatto (distruzione o danno).

### 6.3  Attacco a distanza del nemico

Il nemico (ad esempio un cavaliere) utilizza due nuovi componenti:
- ThrowProjectileComponent per decidere quando lanciare il proiettile;
- TimerComponent per determinare ogni quanto tempo effettuare un lancio.

```java
// Codice da aggiungere al nemico
ThrowProjectileComponent projectileComp = new ThrowProjectileComponent(knight, 2.0f, 0.8f, spear);
knight.addComponent(projectileComp);

TimerComponent knightTimer = new TimerComponent(knight, 5.0f, true);
knightTimer.setOnTimeOver(() -> {
    projectileComp.throwProjectile(); // lancia la freccia ogni 5 secondi
});

knight.addComponent(knightTimer);
```

Spiegazione
- Il TimerComponent scatta ogni 5 secondi (loop = true);
- allo scadere, esegue la lambda onTimeOver(), che richiama il metodo throwProjectile() del ThrowProjectileComponent;
- il ProjectileSystem rileverà poi lo stato di lancio e attiverà il proiettile.

### 6.4  Configurazione dell’InputSystem

Il tasto Spazio viene associato all’azione di attacco del player.
Quando premuto, l’InputSystem comunica al AttackSystem di attivare l’attacco.

```java
// da aggiungere all'InputSystem
inputSystem.bindAction(InputAction.ATTACK, KeyEvent.VK_SPACE);
```
### 6.5  Registrazione dei sistemi nel motore

Infine, aggiungiamo i nuovi sistemi all’engine per aggiornare automaticamente i relativi componenti a ogni frame.

```java
// Sistemi da aggiungere
add(new TimerSystem(this.engine));
add(new AttackSystem(this.engine));
add(new ProjectileSystem(this.engine));
```

Spiegazione
- TimerSystem gestisce i timer e attiva gli eventi periodici;
- AttackSystem controlla le collisioni tra hitbox e bersagli;
- ProjectileSystem gestisce i movimenti e la vita dei proiettili.

### 6.6  In sintesi

| Evento	| Sistema / Componente coinvolto |
| Il giocatore preme Spazio	| InputSystem → AttackComponent |
| Il player attacca con la spada	| AttackSystem rileva la hitbox e applica danno |
| Il timer del nemico scade	| TimerSystem → ThrowProjectileComponent |
| Il nemico lancia una freccia	| ProjectileSystem attiva e muove il proiettile |
| Il proiettile colpisce il player	| InteractionComponent → HealthComponent riduce la vita |

