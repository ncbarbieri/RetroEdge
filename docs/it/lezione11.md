# Lezione 11: Inseguire il Player con il Pathfinding

L’obiettivo di questa lezione è far sì che l’entità nemica insegua il player, usando un algoritmo di pathfinding. Il pathfinding è la tecnica usata per trovare un percorso tra due punti in uno spazio navigabile. È alla base dell’intelligenza artificiale nei giochi. In un gioco 2D a tile, la mappa può essere vista come una griglia: ogni cella è un nodo. Le celle solide possono essere trattate come ostacoli.

Nel gioco, “inseguire” il player significa:
- trovare la posizione del player sulla mappa,
- calcolare il percorso più breve per raggiungerlo,
- muovere il nemico lungo quel percorso.

## 1. Algoritmo A*

A* è un algoritmo di pathfinding, cioè un metodo per trovare il percorso più breve tra due punti su una mappa. È uno degli algoritmi più usati nei videogiochi perché:
- trova il cammino ottimale (se possibile),
- funziona anche con ostacoli,
- è relativamente facile da implementare.

Come abbiamo già visto, in un gioco 2D a tile, la mappa è una griglia di celle (o nodi).  Esempio:

```
      x →
      0   1   2   3   4
y↓  +---+---+---+---+---+
0   | S | . | # | . | G |
1   | . | . | . | . | . |
2   | . | . | . | . | . |
```

- S = punto di partenza (Start)
- G = destinazione (Goal)
- . = spazio vuoto (attraversabile)
- \# = ostacolo (non attraversabile)

L’obiettivo è trovare il cammino più corto da S a G, evitando gli ostacoli. Ogni cella ha un costo e A* assegna ad ogni cella tre valori:


| Simbolo  |	Significato  |
|----------|---------------|
| g(n)	   | Costo dal punto iniziale fino al nodo analizzato n |
| h(n)	   | Costo stimato dal nodo n al goal (euristica) |
| f(n)	   | Costo totale stimato passando per n: f = g + h |

L’euristica h(n) è spesso la distanza di Manhattan:

h(n) = |x<sub>goal</sub> - x<sub>n</sub>| + |y<sub>goal</sub> - y<sub>n</sub>|

Come funziona passo per passo

1. Inizia dal nodo di partenza S e ne calcola il costo
   - g(S) = 0 (parte da lì)
   - h(S) = distanza stimata da S a G
   - f(S) = g + h
2. Inserisce S in una lista chiamata open list
   - L’open list contiene i nodi “da esplorare”
   - La closed list conterrà i nodi “già esplorati”
3. Finché la open list non è vuota:
   - Scegli il nodo con f più basso
   - Se è il goal → hai trovato il cammino
   - Altrimenti:
      - Spostalo nella closed list
      - Esamina i suoi vicini navigabili (es. sopra, sotto, destra, sinistra)
      - Per ogni vicino:
         - Se è già nella closed → salta
         - Se non è nella open:
            - Calcola g, h, f
            - Salva il nodo corrente come genitore
            - Aggiungilo all’open list

Vediamo ora il funzionamento calcolando il cammino minimo della seguente mappa.

```
      x →
      0   1   2   3   4
y↓  +---+---+---+---+---+
0   | S | . | # | . | G |
1   | . | . | . | . | . |
2   | . | . | . | . | . |
```

A* esplora i nodi con f(n) più basso, dove g(n) = passi, h(n) = distanza di Manhattan, f(n)=g(n)+h(n).

1. Start (0,0), g = 0, h = 4, f = 4
   - Vicini:
   - (1,0) → libero g=1, h=3, f=4
   - (0,1) → libero g=1, h=5, f=6
   - openList=[(1,0),(0,1)] → espando (1,0) (f più basso)
2. Nodo (1,0)
   - (2,0) = ostacolo
   - (1,1) = libero, g=2, h=4, f=6
   - openList=[(0,1),(1,1)] → espando (0,1) 
3. Nodo (0,1)
   - (0,2) libero, g=2, h=6, f=8
   - (1,1) già in open (costo migliore)
   - openList=[(1,1),(0,2)] → espando (1,1)
4. Nodo (1,1)
   - (2,1) libero, g=3, h=3, f=6
   - openList=[(2,1),(0,2)] → espando (2,1)
5. Nodo (2,1)
   - (3,1) libero, g=4, h=2, f=6
   - openList=[(3,1),(0,2)] → espando (3,1)
6. Nodo (3,1)
   - (4,1) libero, g=5, h=1, f=6
   - openList=[(4,1),(0,2)] → espando (4,1)
7. Nodo (4,1)
   - (4,0) goal, g=6, h=0, f=6
   - Goal raggiunto!

Cammino finale scelto da A*: (0,0) → (1,0) → (1,1) → (2,1) → (3,1) → (4,1) → (4,0)

```
      x →
      0   1   2   3   4
y↓  +---+---+---+---+---+
0   | S | @ | # | . | G |
1   | . | @ | @ | @ | @ |
2   | . | . | . | . | . |
```

## 2. Algoritmo Jump Point Search (JPS) – Versione HV (solo orizzontale/verticale)

JPS è un algoritmo che accelera il classico A* evitando di esplorare ogni singolo nodo sulla griglia.
Invece di muoversi un passo alla volta, continua a saltare lungo una direzione finché:
- raggiunge il goal
- trova un ostacolo
- esce dalla mappa
- trova un punto critico dove è necessario cambiare direzione o sbloccare un percorso ➜ questo si chiama Jump Point

Un Jump Point è un nodo in cui l’algoritmo interrompe il salto per avviare nuove esplorazioni. Succede quando:
- è possibile cambiare direzione (es. arrivi a un bivio)
- oppure il nodo permette di raggiungere un’altra cella libera che non sarebbe accessibile altrimenti (forced neighbour)

Come funziona JPS HV (orizzontale e verticale)?

1. Parti da un nodo corrente
2. Per ogni direzione (su, giù, sinistra, destra), esegui un salto:
   - Continua a camminare nella stessa direzione finché:
      - non esci dalla mappa
      - non trovi un muro
      - non arrivi al goal
      - non noti un muro di lato che blocca l’accesso a una cella laterale (forced neighbour)
3. Nel caso di un forced neighbour, il nodo corrente è un Jump Point:
   - va aggiunto alla open list
   - verranno esplorati dal Jump Point i salti verso tutte le direzioni

Vediamo un esempio in cui viene individuato un forced neighbour. Questa è una delle condizioni nel metodo jump quando il movimento è orizzontale verso destra:

```java
(isWalkable(neighbour.x, neighbour.y + 1) && !isWalkable(neighbour.x - dx, neighbour.y + 1))
```

```
      x →
      0   1   2   3   4
y↓  +---+---+---+---+---+
0   | S | @ | . | . | . |
1   | # | . | . | . | . |
2   | . | . | . | . | . |
```

L’algoritmo sta saltando verso destra (dx = 1) e si trova sul nodo (1,0).
- la cella sotto (1,1) è libera
- la cella alla sua sinistra (0,1) è un ostacolo.

La cella (1,1) non può essere raggiunta da sinistra, quindi l’unico modo per accedervi è passando da (1,0): per questo motivo (1,0) è un Jump Point e viene aggiunto alla open list. 

Vediamo un esempio:

```
      x →
      0   1   2   3   4
y↓  +---+---+---+---+---+
0   | S | . | . | . | . |
1   | # | . | # | . | . |
2   | . | . | . | . | G |
```

Esecuzione JPS HV:
1. Da (0,0), salto verso destra:
   - (1,0) → libero
2. A (1,0), verifica forced neighbour:
   - Sotto: (1,1) è libera
   - Alla sua sinistra: (0,1) è bloccata (#)
   - Quindi (1,0) è un Jump Point, perché consente accesso a (1,1), che non sarebbe raggiungibile da sinistra.
3. Da (1,1), salto verso destra:
   - (2,1) → ostacolo → fermati
4. Da (1,1), salto verso il basso:
   - (1,2) → libero
5. A (1,2), verifica forced neighbour:
   - Alla sua destra: (2,2) è libera
   - Sopra: (2,1) è bloccata (#)
   - Quindi (1,2) è un Jump Point, perché consente accesso a (2,2), che non sarebbe raggiungibile dall'alto.
6. Da (1,2), salto verso destra:
   - (2,2) → libero
   - (3,2) → libero
   - (4,2) → Goal raggiunto

Percorso trovato da JPS HV: (0,0) → (1,0) → (1,2) → (4,2)

```
      x →
      0   1   2   3   4
y↓  +---+---+---+---+---+
0   | S | @ | . | . | . |
1   | # | . | # | . | . |
2   | . | @ | . | . | G |
```
### Perché JPS è più veloce di A*

Nel classico A*, ogni nodo ha fino a 4 vicini (8 se vengono considerati anche i movimenti in diagonale) da esplorare.

JPS invece:
- Salta direttamente a nodi significativi (Jump Point)
- Evita l’esplorazione di celle intermedie che non portano nuove scelte
- Riduce il numero di nodi nella open list e nelle espansioni

Questo rende JPS **molto più efficiente**, soprattutto su griglie grandi e con pochi ostacoli.

### Approfondimenti

- [Jump-Point Search: Less is More (AAAI 2011)](http://grastien.net/ban/articles/hg-aaai11.pdf) – paper originale
- [A Visual Explanation of Jump Point Search (2013)](https://zerowidth.com/2013/a-visual-explanation-of-jump-point-search/) – guida illustrata e interattiva

## 3. Classi coinvolte

### ChaseComponent

È un componente da assegnare al nemico.
Contiene:
- un riferimento al player (obiettivo da inseguire)
- un riferimento al TileMap (per sapere dove sono gli ostacoli)
- lo stato interno del pathfinding (es. lista dei nodi da seguire)

### ChaseSystem

È un sistema che:
- controlla periodicamente tutti gli Entity con un ChaseComponent
- calcola il percorso dal nemico al player
- aggiorna la direzione del movimento seguendo la path trovata

In pratica: si occupa di fare muovere automaticamente i nemici verso il giocatore.

### PathFinder (classe astratta)

Definisce l’interfaccia comune per qualsiasi algoritmo di pathfinding:
- metodo search(),
- accesso ai nodi,
- tracciamento del percorso trovato

Serve a poter cambiare algoritmo senza cambiare la logica di gioco.

### AStarSearch (estende PathFinder)

Implementa il classico algoritmo A*. Esamina i nodi con la formula f(n) = g(n) + h(n) ed espande tutti i possibili nodi.

Vantaggi:
- semplice
- preciso

Limiti:
- lento su griglie grandi

### JumpPointSearchHV (estende PathFinder)

Implementa il Jump Point Search in versione solo orizzontale/verticale (HV). Ottimizza A* saltando nodi inutili e analizzando solo i “nodi chiave”.

Vantaggi:
- molto più veloce
- meno nodi esplorati

## 4. Esempio: far seguire il player da un nemico

Codice da inserire nel PlayState:

```java
// NUOVO - Creazione nemico "Knight"
Entity knight = new Entity(EntityType.ENEMY, 2);

// Caricamento sprite
CharacterSpritesheet knightSpritesheet = new CharacterSpritesheet("/enemies/spearKnight.sprite");

// Posizione iniziale e velocità
MotionComponent knightPosition = new MotionComponent(knight, 1000, 550, 100.0f);
knight.addComponent(knightPosition);

// Collider per collisioni
ColliderComponent knightCollider = new ColliderComponent(knight, knightSpritesheet.getBoundingBox(), CollisionBehavior.STATIC);
knight.addComponent(knightCollider);

// Componente per inseguire il player
ChaseComponent knightChaser = new ChaseComponent(knight, player, world);
knight.addComponent(knightChaser);

// Sprite e animazioni
SpriteComponent knightSprites = new SpriteComponent(knight, knightSpritesheet, 0.2f, true);
knight.addComponent(knightSprites);

// Vita del nemico
HealthComponent knightHealth = new HealthComponent(knight, 3);
knight.addComponent(knightHealth);

// Aggiungi l'entità nemico alla scena
add(knight);
```


Attiva infine il sistema di inseguimento:

```java
// NUOVO - Sistema di pathfinding nemici
add(new ChaseSystem(this.engine));
```

