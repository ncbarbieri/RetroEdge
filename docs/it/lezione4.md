# Lezione 4 – Tilemap e Camera: dal tilesheet all'open world

Il passo successivo è quello di disegnare una TileMap che faccia da sfondo al personaggio. 
Per ovviare alla poca memoria a disposizione al tempo, veniva usata la tecnica delle Tilemaps, che divenne popolare nello sviluppo di videogiochi. 
La tecnica consisteva nel disegnare il mondo, ovvero la mappa del livello, grazie a una serie di immagini di piccole dimensioni e di forma regolare chiamate Tiles, garantendo così migliori prestazioni e minor impiego di memoria.
Le Tiles venivano mappate per ricostruire il mondo vero e proprio grazie a un array bidimensionale, matrice che poteva essere usata anche per la logica del gioco stesso (per esempio per gestire le collisioni dei personaggi con lo sfondo).
Questo permetteva di creare mondi estesi senza doverne memorizzare l’intera immagine, troppo oneroso per la quantità di RAM disponibile al tempo.

Tutte le Tiles venivano memorizzate in un’unica immagine, chiamata [tileset](https://en.wikipedia.org/wiki/Tile-based_video_game), atlas o spritesheet, che veniva usata soltanto per la porzione corrispondente alla Tile da disegnare.
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
La classe `Tile` rappresenta una singola tessera (tile) del mondo di gioco, che può essere **statica** (una sola immagine) oppure **animata** (più frame). Inoltre, gestisce anche la **solidità** (collisioni) e le **coordinate logiche** all’interno della griglia.

---

### Attributi principali

| Campo           | Tipo              | Descrizione |
|----------------|-------------------|-------------|
| `images`        | `BufferedImage[]` | Contiene uno o più frame associati alla tile |
| `animated`      | `boolean`         | Indica se la tile è animata |
| `solid`         | `boolean`         | Indica se la tile è solida (ostacolo) |
| `solidBox`      | `Rectangle`       | Rettangolo usato per il rilevamento collisioni |
| `row`, `column` | `int`             | Posizione logica della tile nella griglia |
| `tileWidth`     | `int`             | Larghezza della tile in pixel |
| `tileHeight`    | `int`             | Altezza della tile in pixel |

---

### Costruttori

**`Tile(BufferedImage image, boolean solid, int row, int column)`**
- Crea una tile **statica** (un solo frame)
- Imposta `animated = false`

**`Tile(BufferedImage[] animatedTileImages, boolean solid, int row, int column, float timePerFrame)`**
- Crea una tile **animata** con una sequenza di immagini
- Imposta `animated = true`
- Il parametro `timePerFrame` viene passato ma non è usato direttamente qui (si presume che venga gestito altrove)

**Costruttore interno privato**
`private Tile(BufferedImage[] images, boolean solid, int row, int column, boolean animated, float timePerFrame)`
- Inizializza i campi comuni
- Calcola la solidBox, che è la bounding box per le collisioni in coordinate mondo

### Metodi

**`boolean isSolid()`**
- Restituisce true se la tile è un ostacolo
- Può essere usato nei sistemi di collisione

**`Rectangle getSolidBox()`**
- Restituisce una copia (clone) del rettangolo di collisione (solidBox)
- Utile per verificare collisioni senza alterare l’oggetto originale

**`void draw(Graphics2D g, int xOffset, int yOffset, int frameNumber)`**
- Disegna la tile a video, con supporto per scrolling (xOffset, yOffset)
- Se la tile è animata, usa il frameNumber specificato
- Se è statica, disegna sempre `images[0]`

**`boolean isAnimated()`**
- Indica se la tile è animata o statica

## 4. Tool di Configurazione: TileSet Editor

Per semplificare la definizione delle tile animate e delle tile solide prima dell'avvio del gioco, nel package utils si trova un'applicazione di configurazione composta da tre componenti principali:
- **MainApp**: avvia l'interfaccia grafica e gestisce il caricamento/salvataggio del file di configurazione.
- **TilesetPanel**: mostra l'intero tileset e permette di selezionare singole tile; offre due modalità di markup:
   - Solid Mode: cliccando su una tile se ne imposta la forma di collisione (bounding box) tramite un rettangolo regolabile.
   - Animate Mode: selezione di più tile in sequenza e definizione del frameDuration per ciascuna animazione.
- **AnimationPanel**: anteprima in tempo reale delle animazioni definite, con controlli per avviare/pausare e modificare la velocità.

Questa applicazione aiuta a generare un file di mappa testuale che include:
- L'elenco degli indici delle tile animate, ordinato per sequenza.
- I parametri di durata dei frame per ciascuna animazione.
- Le coordinate e dimensioni delle bounding box per le tile solide.

Il file di configurazione risultante può essere caricato dal TileManager all'interno del gioco, semplificando l'inizializzazione di mappe complesse e personalizzate.

## 5. TileMap

La classe `TileMap` rappresenta una **mappa composta da tile**, costruita a partire da:
- Un **tileset** (immagine `.png` + file `.solid.txt` e `.anim.txt`)
- Un file **mappa** (`.txt`) con valori numerici che indicano quali tile usare

Offre funzionalità per:
- Costruzione della griglia logica (`Tile[][]`)
- Calcolo di dimensioni del mondo
- Accesso alle tile, al layout e alla mappa di collisione
- Supporto per tile animate e visibilità della porzione di mappa su schermo

---

### Attributi principali

| Campo             | Tipo                        | Descrizione |
|------------------|-----------------------------|-------------|
| `tileMap`         | `Tile[][]`                 | Griglia bidimensionale di tile |
| `tileWidth`       | `int`                      | Larghezza in pixel di una tile |
| `tileHeight`      | `int`                      | Altezza in pixel di una tile |
| `visibleRows`     | `int`                      | Numero di righe visibili a schermo |
| `visibleCols`     | `int`                      | Numero di colonne visibili a schermo |
| `numberOfFrames`  | `int`                      | Numero massimo di frame nelle tile animate |
| `frameDuration`   | `float`                    | Durata di un frame animato (in secondi) |
| `collidedTiles`   | `Map<Point, Float>`        | Mappa delle tile in collisione con relativo timer |

---

### Costruttore principale

```java
public TileMap(String tilesetFile, String mapFile, float frameDuration)
```

Il costruttore principale esegue le seguenti operazioni:
- Carica il tileset da file
- Carica la mappa leggendo le dimensioni e i valori da mapFile
- Crea una griglia di Tile usando tileset.createTile(...)
- Imposta la durata dei frame e le dimensioni visibili in base a GamePanel.GAME_WIDTH e GAME_HEIGHT

### Caricamento della mappa

La mappa viene salvata in un file .txt. Gli elementi sulla stessa riga vengono separati da una virgola, come nel seguente esempio:

```
1,1,2,2,3
1,0,0,2,3
4,4,4,0,0
```

- Ogni numero rappresenta l’indice di una tile nel tileset
- Il valore 0 indica una tile vuota (null)
- I valori vengono caricati nella griglia tramite loadMap(...)

### Metodi principali

- Dimensioni e accesso

| Metodo                              | Ritorna           | Descrizione                                                                 |
|---------------------------------------|-------------------|-----------------------------------------------------------------------------|
| `getTile(int row, int col)`           | `Tile`            | Restituisce la tile in una posizione specifica                               |
| `getMapWidth() / getMapHeight()`      | `int`             | Numero di colonne / righe nella mappa                                        |
| `getWorldWidth() / getWorldHeight()`  | `int`             | Dimensioni del mondo in pixel                                               |
| `getVisibleCols() / getVisibleRows()` | `int`             | Numero di tile visibili a schermo                                           |

- Tile e proprietà

| Metodo                              | Ritorna           | Descrizione                                                                 |
|---------------------------------------|---------------------|-----------------------------------------------------------------------------|
| `getSolidMap()`                       | `boolean[][]`       | Mappa che indica quali tile sono solide                                     |
| `getCollidedTiles()`                  | `Map<Point, Float>` | Restituisce le tile con cui si è in collisione (e timer)                    |
| `getTileWidth() / getTileHeight()`    | `int`               | Dimensioni di una tile                                                      |
| `getNumberOfFrames()`                 | `int`               | Numero massimo di frame per tile animate                                    |
| `getFrameDuration()`                  | `float`             | Durata in secondi di ogni frame animato                                     |

### Caricamento interno

**`getMap(...)`**
Combina:
- getMapDimensionsFromFile() → conta righe e colonne del file
- loadMap() → carica i valori numerici in una matrice `int[][]`

**`loadMap(...)`**
- Legge ogni riga del file .txt
- Divide i valori con split(",")
- Converte i numeri in interi e li inserisce nella griglia

---

### Creare una mappa con **Tiled**

Per creare facilmente una mappa, si può usare [Tiled](https://www.mapeditor.org/), un editor gratuito per tilemap. La procedura consigliata è la seguente:

1. **Crea un nuovo tileset**  
   - Vai su **Map → New Tileset**  
   - Imposta correttamente la **larghezza** e **altezza delle tile** (es. 32×32)  
   - Aggiungi il file `.png` del tileset  
   - Se ci sono margini o spazi tra le tile, specifica `margin` e `spacing` come in Tiled

2. **Disegna la mappa**  
   - Crea una nuova mappa e inizia a disporre le tile sulla griglia

3. **Esporta la mappa in formato JavaScript**  
   - Vai su **File → Export As...**  
   - Seleziona **Tile map files (*.js)**  
   - Questo genererà un file `.js` contenente un oggetto JavaScript che include la mappa in una variabile, ad esempio:

   ```js
   var map = {
       width: 5,
       height: 4,
       layers: [{
           data: [
               1, 1, 1, 1, 1,
               1, 0, 0, 0, 1,
               1, 0, 2, 0, 1,
               1, 1, 1, 1, 1
           ]
       }]
   };
   ```

4. **Estrai la matrice di tile**  
   - Copia il contenuto dell’array data
   - Riformattalo come griglia con righe separate da newline e valori separati da virgole:
   ```
   1,1,1,1,1
   1,0,0,0,1
   1,0,2,0,1
   1,1,1,1,1
   ```
5. **Salva in un file .txt**
   - Il file può essere caricato direttamente dalla classe TileMap  

### Differenza tra coordinate (riga, colonna) e indice lineare

Quando si accede alle tile nel tileset o nella mappa, è importante distinguere tra:

1. **Coordinate (riga, colonna)**
   - Utilizzate per accedere alla matrice di immagini `tileImages[row][col]`
   - Molto intuitive quando si lavora con una griglia
   ```java
   BufferedImage img = tileImages[2][3]; // Riga 2, colonna 3
   ```

2. **Indice lineare**
   - È il formato usato nella mappa esportata da Tiled (es. `data: [1,1,1,1,1,...]`)
   - Conta da sinistra a destra, riga per riga

Per convertire tra i due formati, si usa il seguente codice:

```java
// Da indice a coordinate
int row = index / cols;
int col = index % cols;

// Da coordinate a indice
int index = row * cols + col;
```

**Nota**: Tiled parte a contare le tile da 1, mentre in Java gli array iniziano da 0, quindi spesso si usa tileNumber - 1 per ottenere l’indice corretto.

## 6. `TileMapComponent`

La classe `TileMapComponent` è un componente ECS che incapsula una `TileMap` e ne gestisce:
- La **logica di animazione per le tile animate**
- Il **rendering ottimizzato** tramite pre-rendering dei frame
- L’accesso ai dati di **collisione**
- Le dimensioni della mappa e della finestra visibile

In altre parole, rappresenta la **vista logica e visuale** di una tilemap nel contesto del sistema di entità.

---

**Attributi principali**

| Campo                  | Tipo                        | Descrizione |
|------------------------|-----------------------------|-------------|
| `tileMap`              | `TileMap`                   | Riferimento alla mappa di gioco sottostante |
| `solidTiles`           | `boolean[][]`               | Mappa delle tile solide, utile per le collisioni |
| `tileMapFrames`        | `BufferedImage[]`           | Array di immagini pre-renderizzate per ogni frame di animazione |
| `tileWidth`, `tileHeight` | `int`                   | Dimensioni di una singola tile |
| `visibleRows`, `visibleCols` | `int`               | Dimensioni visibili a schermo (in tile) |
| `currentFrame`         | `int`                       | Frame corrente dell’animazione della mappa |
| `totalFrames`          | `int`                       | Numero totale di frame disponibili |
| `frameDuration`        | `float`                     | Durata di ogni frame animato |
| `frameTimer`           | `float`                     | Timer per aggiornare l’animazione |
| `collidedTiles`        | `Map<Point, Float>`         | Tile in collisione e relativi timer |

---

**Costruttore**

```java
public TileMapComponent(Entity entity, TileMap tileMap)
```

Inizializza il componente leggendo le informazioni dalla TileMap associata:
- Preleva:
   - Mappa di collisione (solidTiles)
   - Frame e dimensioni tile
   - Timer animazioni
- Calcola le dimensioni visibili a schermo
- Genera le immagini pre-renderizzate della mappa con generateTileMapFrames()

**Rendering ottimizzato con `generateTileMapFrames()`**

Il metodo `generateTileMapFrames()` pre-renderizza ogni frame dell’animazione della mappa in una BufferedImage.

Per ogni frame:
- Crea un’immagine vuota (tileMapImage)
- Cicla su tutte le tile della mappa
- Chiama tile.draw(...) passando il frame corrente (se animata) o 0 (se statica)
- Salva l’immagine generata nell’array tileMapFrames

Questo approccio migliora le prestazioni perché evita di ridisegnare ogni singola tile per ogni frame durante il gioco.

**Logica dell’animazione**

Metodo `update(float deltaTime)`
- Aggiunge deltaTime al frameTimer
- Quando il timer supera frameDuration, aggiorna il frame:
  ```java
  currentFrame = (currentFrame + 1) % totalFrames;
  ```

Metodo `getTileMapImage()`
- Restituisce l’immagine da visualizzare in base al frame corrente:
  ```java
  return tileMapFrames[currentFrame];
  ```

**Gestione collisioni**

Metodo `isSolidTile(int row, int col)`
- Controlla se una tile è solida accedendo alla mappa solidTiles

Metodo `getSolidBox(int row, int col)`
- Restituisce un oggetto Rectangle che rappresenta l’area fisica occupata dalla tile, utile per il sistema di collisioni

Metodo `getCollidedTiles()`
- Restituisce la mappa delle tile con cui si è entrati in collisione, ciascuna associata a un timer (es. per effetti visivi o danno progressivo)

**Altri metodi utili**
| Metodo                              | Descrizione                                   |
|-------------------------------------|-----------------------------------------------|
| getTileMap()                        | Restituisce l’oggetto TileMap associato       |
| getTileWidth() / getTileHeight()    | Dimensioni in pixel di una singola tile       |
| getVisibleRows() / getVisibleCols() | Numero di righe e colonne visibili a schermo  |
| getMapWidth() / getMapHeight()      | Dimensioni totali della mappa in tile         |

Il rendering della mappa avviene all’interno del `RenderingSystem`, che si occupa di disegnare a schermo la `TileMap` pre-renderizzata e, se il **debug** è attivo, evidenzia le **tile solide** e quelle in **collisione**.

---

## 7. Camera e FollowPlayer
Nel contesto di un gioco 2D con mappa più grande della finestra visibile, la **camera** determina **quale porzione del mondo** viene effettivamente visualizzata a schermo.  
L’interfaccia `Camera` e l’implementazione `FollowPlayer` gestiscono lo **scrolling della mappa** in modo che il **giocatore rimanga visibile**, con un comportamento fluido e controllato.

---

### Interfaccia `Camera`

```java
public interface Camera {
    void update(float deltaTime);
    int getxOffset();
    int getyOffset();
}
```
I metodi che devono essere implementati sono:
- **`update(float deltaTime)`**: aggiorna la posizione della camera nel tempo
- **`getxOffset() / getyOffset()`**: restituiscono lo spostamento (offset) della camera rispetto all’origine della mappa

Questi offset sono usati per calcolare la posizione a schermo di ogni elemento del mondo di gioco.

---

### Classe FollowPlayer

Questa classe implementa una camera centrata sul giocatore, ma non in modo rigido: il giocatore si può muovere liberamente in una zona centrale dello schermo, definita da una “box” virtuale.

**Attributi principali**

| Campo                                 | Tipo                | Descrizione                                                                 |
|---------------------------------------|---------------------|-----------------------------------------------------------------------------|
| `pc`                                  | `MotionComponent`   | Posizione attuale del giocatore (X, Y)                                      |
| `xOffset, yOffset`                    | `int`               | Offset della camera rispetto alla mappa                                     |
| `leftBorder, rightBorder`             | `int`               | Margini orizzontali interni alla finestra                                   |
| `topBorder, bottomBorder`             | `int`               | Margini verticali interni alla finestra                                     |
| `maxOffsetX, maxOffsetY`              | `int`               | Offset massimo oltre il quale non si può scrollare                          |
| `frameWidth, frameHeight`             | `int`               | Dimensione dello sprite del giocatore                                       |

**Calcolo dell’offset**

Nel costruttore:
- I bordi interni vengono definiti come percentuali dello schermo (es. 20% a sinistra/destra).
- Gli offset massimi sono calcolati sulla base delle dimensioni della mappa e dello schermo visibile:
  ```java
  maxOffsetX = (mapWidth - visibleCols) * tileWidth;
  maxOffsetY = (mapHeight - visibleRows) * tileHeight;
  ```
- L’offset iniziale è centrato sul giocatore:
  ```java
  xOffset = playerX - GAME_WIDTH / 2;
  yOffset = playerY - GAME_HEIGHT / 2;
  ```

Durante l’aggiornamento (`update(float deltaTime)`)
1. Confronto con i bordi visivi:
   - Se il giocatore si avvicina troppo al bordo destro/sinistro o alto/basso del riquadro interno, l’offset viene aggiornato per spostare la camera.
   - Il movimento della camera avviene solo quando il giocatore esce dai bordi di sicurezza:
     ```java
     if (playerX - xOffset > rightBorder) { ... }
     if (playerX - xOffset < leftBorder) { ... }
     ```
2. Clamping degli offset:
   - Gli offset vengono limitati per evitare che la camera vada fuori dalla mappa:
     ```java
     if (xOffset > maxOffsetX) xOffset = maxOffsetX;
     if (xOffset < 0) xOffset = 0;
     ```
     
**Comportamento visivo**

- Il giocatore può muoversi liberamente in un rettangolo centrale dello schermo (20% - 80% di larghezza/altezza).
- Quando esce da quest’area, la camera lo segue.
- La mappa non viene scrollata oltre i limiti, evitando di mostrare aree vuote.

## 8. Aggiunta di TileMap e Camera al metodo init dello stato

Nel metodo init() dello stato di gioco (es. PlayState), è necessario:
1. Creare l’entità TileManager
2.	Creare il componente TileMapComponent
3.	Aggiungere una Camera che segue il player
4.	Aggiungere il RenderingSystem con la camera attiva

**Codice da inserire in init()**

- Prima del player (creazione mappa)
  ```java
  // Entity: TileManager
  Entity tileManager = new Entity(EntityType.TILEMANAGER, 1);
  TileMap world = new TileMap("/tiles/tilesheet.png", "/maps/map.txt", 0.16f); // 0.16s per frame animato
  TileMapComponent tileMapComponent = new TileMapComponent(tileManager, world);
  tileManager.addComponent(tileMapComponent);
  add(tileManager);
  ```
- Dopo il player (creazione camera)
  ```java
  // Camera
  Camera camera = new FollowPlayer(world, player);
  ```

- Rendering system con supporto alla camera
  ```java
  add(new RenderingSystem(this.engine, camera));
  ```


**Comportamento atteso**
- La tilemap viene disegnata una sola volta per frame, con supporto completo ad animazioni.
- La camera mantiene il giocatore visibile all’interno di un’area centrale.
- Se il debug è attivo:
   - Le tile solide sono evidenziate con un rettangolo pieno.
   - Le tile in collisione recente sono evidenziate in giallo trasparente, con l’intensità legata al tempo rimanente.
