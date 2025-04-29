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

La griglia di visualizzazione è composta di solito da più livelli, allo scopo di creare un mondo graficamente più ricco e usare un numero inferiore di Tiles. I livelli vengono disegnati dal basso verso l’alto, poiché nella prospettiva top-down un oggetto copre tutto ciò che sta al di sotto. Se una roccia appare sopra a diversi tipi di terreno (come erba, sabbia o mattoni) può essere disegnata direttamente sopra alla Tile corrispondente al terreno, invece di doverne prevedere una diversa per ogni tipo di terreno. Se non si rispetta l’ordine dei livelli, però, si rischia di vedere il personaggio principale del gioco camminare sotto un albero o sotto un edificio. La seguente immagine mostra un esempio di entrambe le situazioni: un cavaliere sotto a un albero e un cespuglio su diversi tipi di terreno.
Alla griglia visiva viene fatta corrispondere una griglia logica, usata per gestire le collisioni, per attribuire punti, per innescare determinate azioni se certi oggetti sono posizionati nel modo giusto, per algoritmi di ricerca del cammino minimo, ecc


La classe Tile, invece, rappresenta un singolo elemento (tile) di una mappa di gioco.


## 1 Il Tileset
La classe Tileset rappresenta un'astrazione fondamentale per facilitare l'organizzazione e il recupero di tiles individuali da un unico asset grafico, comunemente noto come tileset o texture atlas, che combina multiple immagini (tiles) in un'unica immagine più grande.
Alla chiamata del costruttore, la classe carica l'immagine del tileset da una risorsa esterna specificata dal parametro fileName. 
Successivamente, analizza l'immagine per determinare il numero di righe e colonne di tiles, basandosi sulle dimensioni fornite dei tiles (tileWidth e tileHeight), nonché su eventuali margini e spaziature (margin e spacing) tra i tiles. 
Tramite l'uso del metodo getSubimage della classe BufferedImage, le immagini dei singoli tiles vengono estratte e memorizzate in una matrice bidimensionale tileImages, secondo la loro disposizione nel tileset originale. 
Questo processo consente l'accesso diretto a ogni immagine del tile tramite coordinate di riga e colonna o un indice unico. 
La classe fornisce metodi per ottenere l'immagine di un tile specifico, sia tramite coordinate di riga e colonna (getTileImage(int row, int col)) che tramite un indice unico (getTileImage(int index)), quest'ultimo calcolando la posizione corrispondente nella matrice bidimensionale basandosi sull'ordinamento dei tiles.

Tileset incapsula un’immagine («tilesheet») e la divide in sotto-immagini di dimensione costante.
Costruttore Tileset(String fileName, int tileW, int tileH, int margin, int spacing)
Carica l’immagine con ImageIO.read.
Calcola righe / colonne in base a dimensioni, margine, spaziatura.
Ritaglia ogni tile con getSubimage e lo salva in tileImages[row][col].
Metodi di utilità
getTileImage(int index) (sequenziale)
getTileImage(int row,int col) (coordinato)

## 3 La Tile
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
mantenere Tile[][] tileMap,
aggiornare l’animazione interna (update avanza currentFrame ogni frameDuration),
disegnare solo l’area visibile (draw calcola la prima riga/colonna visibile tramite gli offset della camera).

Ogni sottoclasse implementa init() per:
istanziare un Tileset,
leggere la matrice numerica,
riempire tileMap con Tile statiche o animate,
impostare parametri di animazione (numberOfFrames, frameDuration).

## 5 UNA CAMERA CHE SEGUE IL PLAYER – FollowPlayer

Implementa Camera con:
quattro bordi interni (20 % del lato finestra) che definiscono la “zona sicura”;
quando il player esce da quella zona la camera aggiorna xOffset/yOffset;
il movimento è limitato da maxOffsetX/Y per non mostrare bordi vuoti.

## 6 METODO init DELLO STATO – AGGIUNTA DI MAPPA E CAMERA


## 7 FLUSSO COMPLETO NEL GAME LOOP

1.	InputSystem legge la tastiera → aggiorna ActionStateManager.
2.	MotionSystem usa i flag di input e la gravità per aggiornare velocità / posizione.
3.	AnimationSystem decide action / direction dal MotionComponent → avanza i frame sprite.
4.	Camera (FollowPlayer) aggiorna gli offset in base al player.
5.	TileManagers avanzano eventuali tile animate.
6.	RenderingSystem disegna nell’ordine:

Risultato: il personaggio scorre in una mappa grande, la telecamera lo segue, le tile animate si muovono e le collisioni solide sono visualizzate in rosso (tasto 0 in debug).
