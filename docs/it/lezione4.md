# Lezione 4 – Tilemap e Camera: dal tilesheet all'open world

Il passo successivo è quello di disegnare una TileMap che faccia da sfondo al personaggio. 
Per ovviare alla poca memoria a disposizione al tempo, veniva usata la tecnica delle Tilemaps, che divenne popolare nello sviluppo di videogiochi. 
La tecnica consisteva nel disegnare il mondo, ovvero la mappa del livello, grazie a una serie di immagini di piccole dimensioni e di forma regolare chiamate Tiles, garantendo così migliori prestazioni e minor impiego di memoria.
Le Tiles venivano mappate per ricostruire il mondo vero e proprio grazie a un array bidimensionale, matrice che poteva essere usata anche per la logica del gioco stesso (per esempio per gestire le collisioni dei personaggi con lo sfondo).
Questo permetteva di creare mondi estesi senza doverne memorizzare l’intera immagine, troppo oneroso per la quantità di RAM disponibile al tempo.

Tutte le Tiles venivano memorizzate in un’unica immagine, chiamata tileset, atlas o spritesheet, che veniva usata soltanto per la porzione corrispondente alla Tile da disegnare.
Per la tecnica delle tilemaps, sono necessarie le seguenti informazioni:
- **Tile size**: la dimensione di una singola Tile in pixels (in orizzontale e in verticale);
- **Image atlas**: l’immagine contenente le Tiles;
- **Map dimension**: le dimensioni della mappa in pixel o in numero di tiles, in orizzontale e in verticale;
- **Visual grid**: una matrice che indica quale Tile deve essere visualizzata. Una cella vuota può essere rappresentata da un valore negativo o nullo;
- **Logic grid**: a seconda del gioco, può essere una matrice di collisione, ecc.

La tecnica di più semplice implementazione è quella delle square-based tilemaps, ovvero quella che fa uso di tiles quadrate. Le tecniche basate sulle rectangular-based tilemaps, che fanno uso di rettangoli, sono poco usate. 

Un altro elemento cardine nella creazione di un videogioco è la componente visiva, ovvero il modo con cui il videogioco visualizza se stesso, le interazioni del giocatore e le sue dirette conseguenze. Per rendere il gioco facilmente fruibile, la visuale deve bilanciare chiarezza, funzionalità e impatto. Un’interfaccia chiara permette infatti di localizzare a colpo d’occhio gli elementi principali del gioco e rende più facile l’interazione. Le prospettive usate comunemente per visualizzare l’area gioco nella tecnica a Tilemaps sono due:
- **top-down**: dall’alto verso il basso (come The Legend of Zelda);
- **side-view**: di lato (come in platform game del genere di Super Mario Bros).

Una Tilemap può essere di dimensioni pari o superiori a quelle dello schermo. Nel primo caso la tilemap si dice statica (static tilemap) perché non bisogna farla scorrere per visualizzarla tutta, come nel caso di Pacman. Nel secondo caso, invece, la tilemap si dice a scorrimento (scrolling tilemap) poiché in ogni istante di tempo solo una parte del mondo viene visualizzata. La porzione disegnata può seguire il personaggio principale, come nei giochi platform o negli ARPG, oppure può essere scelta dal giocatore stesso, come nei giochi di strategia e di simulazione, controllando la posizione della macchina da presa che virtualmente inquadra la scena.

Per posizionare la macchina da presa in una scrolling tilemap, si rende necessario tradurre le coordinate del mondo (ovvero la posizione di un oggetto nella mappa del mondo o del livello) nelle coordinate dello schermo (ovvero la posizione dello schermo dove l’oggetto verrà disegnato). Le coordinate del mondo possono essere espresse in Tiles (riga e colonna della matrice) o in pixel sulla mappa, a seconda del gioco. Si rende così necessario conoscere la posizione della macchina da presa, determinante per calcolare quale porzione del mondo disegnare. Nello specifico, se la macchina da presa punta all’angolo in alto a sinistra, le coordinate si traducono nel seguente modo:
- **x_schermo = x_mondo - x_camera;**
- **y_schermo = y_mondo - y_camera;**

Per visualizzare gli oggetti sullo schermo, è sufficiente una scansione parziale della matrice: solo le Tiles visibili vengono disegnate, sottraendo le coordinate della macchina da presa. Disegnare le Tile nascoste sarebbe inutile e peggiorerebbe le prestazioni.

La griglia di visualizzazione è composta di solito da più livelli, allo scopo di creare un mondo graficamente più ricco e usare un numero inferiore di Tiles. I livelli vengono disegnati dal basso verso l’alto, poiché nella prospettiva top-down un oggetto copre tutto ciò che sta al di sotto. Se una roccia appare sopra a diversi tipi di terreno (come erba, sabbia o mattoni) può essere disegnata direttamente sopra alla Tile corrispondente al terreno, invece di doverne prevedere una diversa per ogni tipo di terreno. Se non si rispetta l’ordine dei livelli, però, si rischia di vedere il personaggio principale del gioco camminare sotto un albero o sotto un edificio. 
Alla griglia visiva viene fatta corrispondere una griglia logica, usata per gestire le collisioni, per attribuire punti, per innescare determinate azioni se certi oggetti sono posizionati nel modo giusto, per algoritmi di ricerca del cammino minimo, ecc

## 1. Tileset
La classe `Tileset` rappresenta una raccolta di immagini (tile) caricate da un'unica immagine sorgente. Fornisce funzionalità per:

- Estrazione delle singole tile
- Gestione delle **tile solide**
- Gestione delle **tile animate**
- Creazione di oggetti `Tile` per creare la mappa

Quando viene istanziata una classe `Tileset`, viene caricata un’immagine completa (sprite sheet `.png`) che contiene tutte le tile disposte in una **griglia di righe e colonne**.  A volte le tile non sono disposte una accanto all’altra senza interruzioni. Vengono introdotti due parametri importanti: il margine e la spaziatura.

Il **margine** è lo spazio (in pixel) che si trova **tra il bordo esterno dell'immagine** e la prima tile.  
In pratica, è uno "cuscinetto" che circonda tutte le tile e che va **ignorato** durante il ritaglio.

La **spaziatura** è la distanza (in pixel) **tra una tile e l'altra** all'interno del tileset.  
Serve per evitare artefatti grafici (bleeding) durante il rendering, specialmente se l’immagine viene scalata o filtrata.

In un tileset con 3 tile in una riga, dove ogni tile è larga 32 pixel, con:

- `margin = 4`
- `spacing = 2`

L'immagine avrà la seguante struttura (in pixel):

|––4px––|–32–|–2–|–32–|–2–|–32–|––4px––|

Totale larghezza = `4 + 32 + 2 + 32 + 2 + 32 + 4 = 106 px`

Nel costruttore della classe `Tileset`, questi parametri vengono usati per calcolare dove ritagliare le tile:
In questo modo, ogni tile viene estratta nel punto corretto ignorando margini e spazi vuoti.

A seconda del costruttore usato, le righe e le colonne vengono determinate in due modi:

1. **Tramite dimensioni note** (`tileWidth`, `tileHeight`, opzionalmente `margin` e `spacing`):  
   - L'immagine viene suddivisa automaticamente in base alle dimensioni dichiarate.  
   - Le tile vengono ritagliate con `getSubimage(...)`.

2. **Tramite file `.solid.txt`**:  
   - Se il costruttore non riceve esplicitamente le dimensioni delle tile, viene letto un file di testo con righe composte da caratteri `1` e `0`.  
   - Ogni riga rappresenta una **riga di tile** e la **lunghezza della riga** determina il numero di colonne.  
   - Esempio:
     ```
     00100
     11100
     00011
     ```
     In questo caso, il tileset ha `3` righe e `5` colonne.  
   - L’altezza e la larghezza di ogni tile si calcolano dividendo la dimensione totale dell’immagine per il numero di righe e colonne.

Una volta nota la griglia, tutte le tile vengono estratte e memorizzate nella matrice `tileImages[row][col]`, che consente un accesso diretto alle immagini.

---

### Attributi principali

| Campo                      | Tipo                        | Descrizione |
|---------------------------|-----------------------------|-------------|
| `tileset`                 | `BufferedImage`             | L’immagine sorgente contenente tutte le tile |
| `tileImages`              | `BufferedImage[][]`         | Matrice delle immagini delle singole tile |
| `solidTiles`              | `boolean[][]`               | Mappa delle tile solide (`true` = solida) |
| `animatedTiles`           | `Map<Point, List<Point>>`   | Mappa delle tile animate e dei loro frame |
| `rows`, `cols`            | `int`                       | Numero di righe e colonne nel tileset |
| `tileWidth`, `tileHeight` | `int`                       | Dimensione in pixel di ciascuna tile |
| `numberOfFrames`          | `int`                       | Numero massimo di frame per una tile animata |

---

### Costruttori

**`Tileset(String fileName, int frameWidth, int frameHeight)`**

- Carica un tileset assumendo tile di dimensione fissa
- Usa come default `margin = 0`, `spacing = 0`

**`Tileset(String fileName)`**

- Carica il tileset leggendo righe e colonne da un file `.solid.txt`

**`Tileset(String fileName, int tileWidth, int tileHeight, int margin, int spacing)`**

- Supporta tileset con margini e spaziatura (utile per sprite sheet esportati da **Tiled**)

Tutti i costruttori caricano:

- L’immagine principale
- Le immagini delle tile (`tileImages`)
- Le informazioni di solidità (`solidTiles`)
- Le informazioni di animazione (`animatedTiles`)

---

### Metodi di supporto

**`loadSolidMap(String fileName)`**

- Legge da un file `.solid.txt` quali tile sono solide (`1`) e quali no (`0`)
- Ogni riga del file rappresenta una riga di tile

**`loadAnimationData(String fileName)`**

- Legge da un file `.anim.txt` la lista di frame per ciascuna tile animata
- Sintassi del file: `(y,x)->(frameY1,frameX1)(frameY2,frameX2)…`

**`getTileDimensionsFromFile(String fileName)`**

- Legge il file `.solid.txt` per determinare il numero di righe e colonne

---

### Gestione delle animazioni

Le tile animate sono definite da una mappa:
```java
Map<Point, List<Point>> animatedTiles;
```
- **Chiave**: coordinate della tile animata
- **Valore**: lista di frame (coordinate)

Ogni frame viene recuperato da tileImages.

Il metodo **`createTile(...)`** restituisce un oggetto Tile, che può essere:
- **Statico**: una singola immagine
- **Animato**: array di immagini e durata per frame

Controlla anche la solidità usando solidTiles.

Esempi:

```java
// Tile animata
return new Tile(animatedImages, isSolid, row, col, duration);

// Tile statica
return new Tile(tileImage, isSolid, row, col);
```

La classe `Tileset` include diversi metodi getter per accedere ai dati interni. Ecco una panoramica:

| Metodo                              | Ritorna           | Descrizione                                                                 |
|-------------------------------------|-------------------|-----------------------------------------------------------------------------|
| `getRows()`                         | `int`             | Numero di righe di tile nel tileset                                        |
| `getCols()`                         | `int`             | Numero di colonne di tile nel tileset                                      |
| `getTileWidth()`                    | `int`             | Larghezza di ogni tile in pixel                                            |
| `getTileHeight()`                   | `int`             | Altezza di ogni tile in pixel                                              |
| `getTileImage(int row, int col)`   | `BufferedImage`   | L'immagine della tile in posizione `(row, col)`                            |
| `getTileImage(int index)`          | `BufferedImage`   | L'immagine della tile con indice lineare (scorrendo da sinistra a destra) |
| `getAnimationFrames(tileX, tileY)` | `List<Point>`     | Lista dei frame animati associati alla tile, oppure `null` se non animata |
| `getNumberOfFrames()`              | `int`             | Numero massimo di frame nelle animazioni                                   |

Questi metodi permettono l’accesso controllato ai dati e sono utili per altri sistemi (come `TileManager`, `CollisionSystem`, o `Renderer`) che devono utilizzare il tileset.

## 3. Tile
La classe Tile, invece, rappresenta un singolo elemento (tile) di una mappa di gioco.

Contiene uno o più frame (BufferedImage[] images).
Può essere statica (1 frame) o animata (n frame).
Attributi principali:
solid → se vero la tile non è attraversabile.
solidBox → rettangolo di collisione già posizionato nel mondo.
collision / collisionCounter → usati solo in debug per tingere la tile in giallo quando riceve un urto.
Metodi chiave
draw(Graphics2D g, xOff, yOff, currentFrame) disegna la tile nella posizione corretta sottraendo gli offset di camera.
collides() da chiamare quando qualcosa sbatte nella tile: abilita l’overlay giallo in debug.

## 4 GESTIRE UNA MAPPA COMPLETA – TileManager (astratto)

Fornisce lo scheletro per:
caricare da file di testo (metodi loadMap e loadSolidMap),
mantenere `Tile[][] tileMap`,
aggiornare l’animazione interna (update avanza currentFrame ogni frameDuration),
disegnare solo l’area visibile (draw calcola la prima riga/colonna visibile tramite gli offset della camera).

Ogni sottoclasse implementa init() per:
istanziare un Tileset,
leggere la matrice numerica,
riempire tileMap con Tile statiche o animate,
impostare parametri di animazione (numberOfFrames, frameDuration).

## 5 Tool di Configurazione: TileSet Editor

Per semplificare la definizione delle tile animate e delle tile solide prima dell'avvio del gioco, è stata creata un'applicazione di configurazione composta da tre componenti principali:
- MainApp: avvia l'interfaccia grafica e gestisce il caricamento/salvataggio del file di configurazione.
- TilesetPanel: mostra l'intero tileset e permette di selezionare singole tile; offre due modalità di markup:
- Solid Mode: cliccando su una tile se ne imposta la forma di collisione (bounding box) tramite un rettangolo regolabile.
- Animate Mode: selezione di più tile in sequenza e definizione del frameDuration per ciascuna animazione.
- AnimationPanel: anteprima in tempo reale delle animazioni definite, con controlli per avviare/pausare e modificare la velocità.

Questa applicazione aiuta a generare un file di mappa testuale che include:
- L'elenco degli indici delle tile animate, ordinato per sequenza.
- I parametri di durata dei frame per ciascuna animazione.
- Le coordinate e dimensioni delle bounding box per le tile solide.

Il file di configurazione risultante può essere caricato dal TileManager all'interno del gioco, semplificando l'inizializzazione di mappe complesse e personalizzate.

## 6 UNA CAMERA CHE SEGUE IL PLAYER – FollowPlayer

Implementa Camera con:
quattro bordi interni (20 % del lato finestra) che definiscono la “zona sicura”;
quando il player esce da quella zona la camera aggiorna xOffset/yOffset;
il movimento è limitato da maxOffsetX/Y per non mostrare bordi vuoti.

## 7 METODO init DELLO STATO – AGGIUNTA DI MAPPA E CAMERA


## 8 FLUSSO COMPLETO NEL GAME LOOP

InputSystem legge la tastiera → aggiorna ActionStateManager.
MotionSystem usa i flag di input e la gravità per aggiornare velocità / posizione.
AnimationSystem decide action / direction dal MotionComponent → avanza i frame sprite.
Camera (FollowPlayer) aggiorna gli offset in base al player.
TileManagers avanzano eventuali tile animate.
RenderingSystem disegna nell’ordine:

Risultato: il personaggio scorre in una mappa grande, la telecamera lo segue, le tile animate si muovono e le collisioni solide sono visualizzate in rosso (tasto 0 in debug).
