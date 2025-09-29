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
S . . . . . . .
. # # # . # # .
. . . . . . . G
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

