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


