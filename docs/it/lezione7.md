# Lezione 7 – Sistema di Interfaccia Utente (UI)

L’interfaccia utente (UI) è la componente visiva che permette al giocatore di ricevere feedback durante la partita, come il numero di gemme raccolte, la salute residua o lo stato del gioco. In questa lezione analizziamo il sistema UI modulare di RetroEdge, basato sull’architettura ECS (Entity-Component-System).

## 1. UIElement – Elemento UI base

UIElement è una classe astratta che rappresenta qualsiasi oggetto dell’interfaccia utente (es. etichette, barre, icone). Serve come superclasse per tutti gli elementi visivi UI.

Caratteristiche:
- Posizione (x, y): coordinate sullo schermo, in pixel.
- Layer (int): stabilisce la priorità di rendering (elementi con layer maggiore sono disegnati sopra).
- Visibilità (visible): abilita/disabilita il disegno senza rimuovere l’elemento.
- Metodi astratti:
   - update(float deltaTime): per animazioni o logica temporale.
   - render(Graphics2D g2d): per disegnare l’elemento a schermo.

Note progettuali
- Gli UIElement non sono entità, ma possono essere aggiornati tramite entità che li referenziano.
- Il layer consente una gestione sofisticata della sovrapposizione visiva.

## 2. UIComponent – Collegamento tra entità e UI

UIComponent è un componente ECS che consente a un’entità di essere collegata a un UIElement.

Caratteristiche:
- Contiene un riferimento a un UIElement.
- Espone getter e setter per manipolare dinamicamente l’elemento UI.

Note progettuali:
- È un componente passivo: non contiene logica ma solo dati.
- Può essere utilizzato da qualsiasi entità per aggiornare dinamicamente l’interfaccia visiva.

## 3. UISystem – Sistema ECS per aggiornare la UI

UISystem è un sistema ECS che gestisce l’aggiornamento degli elementi UI collegati a entità tramite UIComponent.

Caratteristiche:
- Scorre tutte le entità con UIComponent.
- Richiama update(deltaTime) sull’elemento UI associato.

Note progettuali
- Non disegna nulla: il rendering è centralizzato nel metodo render() dell’Engine.
- Permette agli UIElement di evolvere nel tempo in modo autonomo (ad esempio una barra che si svuota o un’etichetta lampeggiante).

## 4. Label – Etichetta testuale dinamica

Label è una sottoclasse concreta di UIElement, pensata per visualizzare testo sullo schermo.

Caratteristiche:
- Testo dinamico (text): modificabile a runtime.
- Font e colore: completamente personalizzabili.
- Metodo setText(String): per aggiornare il contenuto visualizzato.

Note progettuali
- Utile per punteggi, contatori, notifiche, dialoghi.
- Supporta l’ereditarietà e la gestione del layer per la corretta visualizzazione.

## 5. Architettura completa del sistema UI
| Classe	                  | Ruolo	                                                                 |
|---------------------------|------------------------------------------------------------------------|
| UIElement	                | Classe base per qualsiasi elemento visuale UI	                         |
| Label	                    | UIElement specializzato nella visualizzazione di testo	               |
| UIComponent	              | Componente ECS che collega un’entità a un elemento UI	                 |
| UISystem	                | Sistema ECS che aggiorna ogni elemento UI collegato a entità	         |


## 6. Esempio: visualizzare il numero di gemme raccolte

Vogliamo mostrare in alto a sinistra lo stato delle gemme raccolte dal giocatore. Procediamo in questo modo:

1. Aggiungiamo il font nel metodo init() del PlayState
```java
Font font;
try {
    font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/myFont.otf"));
    font = font.deriveFont(24f);
    GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
} catch (Exception e) {
    font = new Font("Arial", Font.PLAIN, 24);
}
// Register the font
GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
ge.registerFont(font);
```

2. Istanziamo l’etichetta
```java
Label gemLabel = new Label(25, 40, 1, "Gems: 0", font, Color.white);
gemLabel.show();
```

3. Colleghiamo l'etichetta a un’entità con UIComponent
```java
Entity uiPlayer = new Entity(EntityType.UI, 6);
UIComponent pComponent = new UIComponent(uiPlayer, gemLabel);
uiPlayer.addComponent(pComponent);
add(uiPlayer);
```

4. Quando il giocatore raccoglie una gemma, modifichiamo il testo della label modificando il codice dell'InteractionComponent
```java
gemLabel.setText("Gems: " + playerComponent.getGems());
```

5. Aggiungiamo il sistema per l'aggiornamento dell'interfaccia utente
```java
add(new UISystem(this.engine, camera));
```
