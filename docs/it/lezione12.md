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
| Oggetto distruttibile	| Emette particelle o lascia cadere un bonus. |

