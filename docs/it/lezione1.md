# Lezione 1: Introduzione agli ARPG e il Game Loop

# 1.1 Storia dei videogiochi: dalle origini alle icone a 16 bit

## Dalle prime sperimentazioni accademiche alla nascita delle console (anni ’40 – primi ’70)

- **Nimatron (1939–1940)**  
  Ideato da Edward Condon, era un dispositivo a relè progettato per il gioco da tavolo *Nim*, presentato alla Fiera Mondiale di New York. Pur non essendo un “videogioco” vero e proprio, è un precursore dei futuri giochi elettronici.

- **Cathode-Ray Tube Amusement Device (1948)**  
  Progettato da Thomas T. Goldsmith Jr. ed Estle Ray Mann, utilizzava un tubo a raggi catodici per tracciare la traiettoria di un proiettile contro sagome plastiche poste sullo schermo. È considerato uno dei primi dispositivi di gioco elettronico.

- **Tennis for Two (1958)**
 William Higinbotham, presso il Brookhaven National Laboratory, realizzò questo gioco di “tennis” su un oscilloscopio, con l’intento di intrattenere. A differenza dei progetti precedenti, non aveva scopi di ricerca o promozione tecnologica.

<div align="center">
  <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Tennis_For_Two_on_a_DuMont_Lab_Oscilloscope_Type_304-A.jpg/440px-Tennis_For_Two_on_a_DuMont_Lab_Oscilloscope_Type_304-A.jpg" alt="Tennis for Two" width="400"/>
  <p><em>Figura 1: Tennis for Two (1958) – Fonte: Wikimedia Commons</em></p>
</div>

- **Spacewar! (1962)**  
  Sviluppato da Steve Russell e altri al MIT per il minicomputer PDP-1, è uno dei giochi digitali più influenti. Diffuso negli ambienti universitari grazie al codice sorgente condiviso, ispirò i primi coin-op commerciali (*Computer Space* e *Galaxy Game*, 1971).

- **La “Brown Box” e Magnavox Odyssey (1967–1972)**  
  Ralph Baer sviluppò il prototipo della prima console domestica, la “Brown Box”, poi commercializzata da Magnavox come *Odyssey* (1972). Fu il primo dispositivo che permetteva di giocare in casa.

---

## L’era di Pong, Atari e l’ascesa delle console a cartuccia (anni ’70)

- **Pong (1972)**  
  Nato come esperimento di Allan Alcorn per Atari, *Pong* divenne un successo nelle sale giochi, dimostrando la redditività del videogioco come intrattenimento di massa.

- **Atari 2600 (1977)**  
  Inizialmente noto come Atari VCS, rese popolari le cartucce intercambiabili e portò i videogiochi nelle case. Conversioni come *Space Invaders* (1980) contribuirono alla crescita esponenziale dell’industria.

---

## La nascita degli home computer (fine anni ’70 – inizio anni ’80)

L’uso dei microcomputer domestici aprì a un nuovo mercato di software e videogiochi amatoriali:

- **La “Trinità del 1977”**  
  Apple II, Commodore PET e Tandy TRS-80 resero il computer accessibile al grande pubblico.

- **Commodore 64 (1982)**  
  Con 64 KB di RAM e un prezzo competitivo, divenne uno dei computer più venduti al mondo, con una vivace scena di sviluppo "casalingo".

- **Tandy TRS-80**  
  Prodotto da Radio Shack, fu tra i primi veri computer personali. Per questa piattaforma uscì *Dungeons of Daggorath* (1982), uno dei primi action RPG.

Gli home computer offrivano più libertà creativa rispetto alle console, favorendo la nascita di generi ibridi come le avventure testuali, i simulatori e gli action RPG.

---

## *Dungeons of Daggorath* (1982) e la genesi degli action RPG

### Meccaniche degli action RPG

- **ARPG (Action Role-Playing Game)**  
  Unisce crescita del personaggio e gestione delle statistiche dei GDR tradizionali con combattimenti in tempo reale, in cui il giocatore deve agire attivamente.

### *Dungeons of Daggorath*

- **Dungeons of Daggorath (1982)**  
  Realizzato per TRS-80, è uno dei primi ARPG. Propone un dungeon in 3D wireframe, combattimenti in tempo reale e gestione di parametri vitali (respiro, ferite). La fusione tra ruolo e azione anticipa molte caratteristiche future del genere.

---

## La rinascita dopo il “crash” del 1983: NES e console war

- **Il crollo dell’industria (1983)**  
  Il mercato videoludico nordamericano crollò a causa della saturazione e della scarsa qualità di molti titoli (es. *E.T.* per Atari 2600), con vendite in calo da 3 miliardi a 100 milioni.

- **Nintendo Entertainment System (1985)**  
  Nintendo rilanciò il settore con il NES e *Super Mario Bros.* (1985), riconquistando la fiducia dei consumatori.

- **La “console war”: Sega vs. Nintendo (fine anni ’80)**  
  Il lancio del Sega Mega Drive (1988–89) spinse Nintendo a evolvere con titoli e hardware migliori, avviando una forte competizione che caratterizzò gli anni ’90.

---

## *The Legend of Zelda* e l’impatto sugli action RPG (1986)

- **The Legend of Zelda (1986)**  
  Sviluppato da Shigeru Miyamoto per il Famicom Disk System (e poi NES), univa esplorazione, enigmi e combattimento in tempo reale con elementi GDR. Presentava una struttura *open world*, permettendo libertà di movimento e progressione non lineare.  
  Questo ibrido influenzò profondamente lo sviluppo degli ARPG successivi.

---

## L’avvento dei 16 bit e il trionfo di *A Link to the Past* (1990–1991)

- **Super Famicom / SNES (1990)**  
  Nintendo rispose al Mega Drive con il Super Nintendo, che offriva grafica e audio migliorati per esperienze di gioco più ricche.

- **The Legend of Zelda: A Link to the Past (1991)**  
  Evoluzione del primo *Zelda*, offriva un vasto mondo esplorabile, combattimenti in tempo reale e dungeon intricati. Considerato uno dei titoli simbolo dell’era 16-bit e un punto di riferimento per action-adventure e ARPG.

---

## Conclusioni

Dalle ricerche accademiche e prototipi sperimentali, passando per l’home computing e le prime console, fino ai grandi classici degli anni '90, i videogiochi hanno attraversato un'evoluzione culturale profonda.  
Giochi come *Dungeons of Daggorath* e *The Legend of Zelda* hanno tracciato la via per il genere action RPG, culminando con capolavori come *A Link to the Past* (1991).  
Un viaggio che ha trasformato un fenomeno di nicchia in uno dei pilastri dell’intrattenimento globale.

---


# 1.2 Il Game Loop

Il Game Loop è il cuore di ogni videogioco ed è composto da tre fasi principali:

1. **Gestione degli input dell'utente** (tastiera, mouse, controller).
2. **Aggiornamento dello stato di gioco** (posizioni, collisioni, logica).
3. **Rendering della scena aggiornata** sullo schermo.

Questo ciclo si ripete continuamente per tutta la durata del gioco.

## Threading e sincronizzazione

Per garantire fluidità al gioco, spesso si utilizza un thread dedicato per il Game Loop. Il threading consente di separare l'esecuzione delle operazioni di gioco (aggiornamenti e rendering) da quelle del sistema operativo.

```java
@Override
public void run() {
    double drawInterval = 1000000000 / FPS; // intervallo tra ogni frame
    double delta = 0;
    long lastTime = System.nanoTime();
    long currentTime;

    while(gameThread != null) {
        currentTime = System.nanoTime();
        delta += (currentTime - lastTime) / drawInterval;
        lastTime = currentTime;

        if(delta >= 1) {
            update(); // aggiorna la logica del gioco
            repaint(); // richiama il metodo paintComponent
            delta--;
        }
    }
}
```

## Calcolo del Delta Time

Il delta time è il tempo trascorso tra due frame consecutivi e viene utilizzato per aggiornare correttamente le posizioni e animazioni, indipendentemente dalla velocità del sistema su cui gira il gioco.

### Esempio di calcolo del delta time:

```java
long lastTime = System.nanoTime();

while(running) {
    long currentTime = System.nanoTime();
    float deltaTime = (currentTime - lastTime) / 1e9f; // tempo in secondi
    lastTime = currentTime;

    update(deltaTime); // logica aggiornata con deltaTime
    render();
}
```

## Classi di esempio

- **`MainECS.java`**: Classe principale che gestisce inizializzazione e ciclo del gioco.
- **`GamePanel.java`**: Pannello su cui vengono renderizzati gli elementi grafici del gioco.

> Le classi complete sono disponibili nella cartella dei sorgenti del progetto.

## Riferimenti e fonti

- Steven L. Kent, *The Ultimate History of Video Games*, Three Rivers Press, 2001.
- Documentazione storica di Nintendo, Sega, Atari e Tandy/Radio Shack.
- Schede e manuali originali di *Dungeons of Daggorath* (1982) per TRS-80.
- Approfondimenti su *The Legend of Zelda* e *A Link to the Past* dagli archivi Nintendo.
