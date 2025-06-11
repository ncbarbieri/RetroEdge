# Lezione 10: Stati di Gioco e Transizioni

Durante una normale sessione di gioco, si attraversano diversi momenti distinti:
- la schermata del titolo, con logo e musica di sottofondo;
- il menu principale, dove il giocatore può iniziare una partita, caricarla o modificare le opzioni;
- la fase di gioco attiva, dove si svolge l’azione vera e propria;
- la schermata di pausa, che sospende temporaneamente l’azione;
- le cutscene narrative che interrompono temporaneamente il controllo diretto del giocatore;
- e così via.

Immagina che il tuo videogioco sia uno spettacolo teatrale messo in scena su un palco.
- Il palcoscenico è la finestra del gioco: lo spazio in cui tutto viene rappresentato, ciò che il giocatore vede.
- Il motore di gioco (Engine) è il regista invisibile: decide quale scena mettere in scena, quali attori devono entrare, quali luci e suoni attivare, e quando cambiare scena.
- Gli attori sono le entità del gioco: personaggi, nemici, oggetti, elementi UI.
- Gli scenari e fondali sono i sistemi attivi: gestiscono il rendering, le collisioni, l’input, l’audio.
- Ogni scena dello spettacolo corrisponde a uno stato di gioco, come il menu, il gameplay o la pausa.

Quando si cambia stato, non si ricomincia tutto da capo: come in teatro si cambia la scenografia, entrano nuovi attori, parte una nuova musica, ma il regista (il motore) resta al comando, orchestrando tutto dietro le quinte.
Ogni fase ha comportamenti, interfacce e regole diversi e indipendenti. Per evitare che tutto venga gestito da un unico blocco di codice, disordinato e difficile da mantenere, il motore RetroEdge adotta un approccio a stati.
Uno stato di gioco si occupa principalmente di preparare e organizzare l’ambiente di gioco per una determinata fase. In particolare, uno stato:
- inizializza le risorse necessarie (immagini, suoni, entità, sistemi);
- configura le entità e i componenti da utilizzare;
- definisce gli effetti di transizione visiva, come fade-in e fade-out;
- rilascia le risorse quando il gioco passa a un altro stato.

Sebbene ogni stato abbia una sua configurazione indipendente, alcune entità possono persistere da uno stato all’altro.
Un esempio tipico è il giocatore (player): una volta creato, può essere mantenuto anche durante cutscene, schermate di caricamento, o il passaggio da un livello all’altro.

## 1. La classe base GameState

Ecco una sintesi del ruolo della classe GameState, che è astratta e usata come base per tutti gli stati concreti:

```java
public abstract class GameState {
	protected Engine engine;

	public GameState(Engine engine) {
		this.engine = engine;
	}

	public abstract void init();
	public abstract void cleanup();

	public TransitionEffect getEnterTransition() {
		return null;
	}

	public TransitionEffect getExitTransition() {
		return null;
	}

	public void add(Entity entity) {
		engine.addEntity(entity);
	}

	public void add(BaseSystem system) {
		engine.addSystem(system);
	}
}
```

Metodi principali:

| Metodo	| Scopo |
|---------|-------|
| init()	| Inizializza lo stato e registra sistemi, entità, camera, elementi UI |
| cleanup()	| Rilascia risorse al termine dello stato |
| getEnterTransition()	| Restituisce un effetto da eseguire quando si entra nello stato |
| getExitTransition()	| Restituisce un effetto da eseguire quando si esce dallo stato |
| add(Entity) e add(BaseSystem)	| Helper per aggiungere rapidamente entità e sistemi all’engine |

## 2. Transizioni: TransitionEffect, FadeIn, FadeOut

Per rendere più fluide le transizioni tra stati, RetroEdge supporta una gerarchia di effetti animati.

### 2.1 TransitionEffect

La classe TransitionEffect è una classe astratta, progettata per rappresentare effetti visivi temporanei che accompagnano il passaggio da uno stato di gioco a un altro.

Attributi:

| Attributo	| Tipo	| Descrizione |
|-----------|-------|-------------|
| duration	| float	| Durata complessiva dell’effetto (in secondi). |
| timeElapsed	| float	| Tempo già trascorso dall’inizio della transizione. |
| complete	| boolean	| Indica se l’effetto è terminato (true) oppure ancora in corso (false). |

Metodi:

| Metodo	| Tipo	| Descrizione |
|---------|-------|-------------|
| ```TransitionEffect(float duration)```	| Costruttore	| Inizializza l’effetto impostando la durata e azzerando il tempo trascorso. |
| ```void update(float deltaTime)```	| Ordinario	| Aggiunge deltaTime al tempo trascorso e imposta complete su true se si supera la durata. |
| ```abstract void render(Graphics2D g, int width, int height)```	| Astratto	| Deve essere implementato dalle sottoclassi per disegnare l’effetto sullo schermo. |
| ```boolean isComplete()```	| Ordinario| 	Restituisce true se l’effetto è terminato. Usato dal motore per sapere quando concludere la transizione. |

La classe TransitionEffect serve a:
- astrarre la logica comune di tutte le transizioni (gestione del tempo, completamento);
- delegare alle sottoclassi la responsabilità del disegno (es. fade, slide, dissolvenze…);
- semplificare il codice del motore, che può trattare tutte le transizioni in modo uniforme.

### 2.2 FadeInEffect

La classe FadeInEffect è una sottoclasse concreta di TransitionEffect, che implementa una dissolvenza graduale da nero a trasparente, tipica degli ingressi scenici nei videogiochi.

Viene utilizzata come effetto visivo di entrata in uno stato, per rendere la transizione morbida e visivamente gradevole.

Metodi

| Metodo	| Tipo	| Descrizione |
|---------|-------|-------------|
| FadeInEffect(float duration)	| Costruttore	| Inizializza la durata dell’effetto. Passa il parametro al costruttore della superclasse TransitionEffect. |
| void render(Graphics2D g, int width, int height)	| Override	| Disegna un rettangolo nero con opacità decrescente, in base al tempo trascorso. |
| void update(float deltaTime) | Override | Aggiorna l'opacità a seconda del tempo trascorso |

Il funzionamento è il seguente:

1.	Calcola l’opacità in base al tempo trascorso nel metodo update:
   
     ```float alpha = 1.0f - (timeElapsed / duration);```
  	- All’inizio (timeElapsed = 0) → alpha = 1.0 (nero pieno)
    - Alla fine (timeElapsed = duration) → alpha = 0.0 (completamente trasparente)

2.	Nel metodo render(), imposta l’opacità nel contesto grafico:

     ```g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));```

3.	Disegna un rettangolo nero a schermo intero:

     ```g.setColor(Color.BLACK);```
     ```g.fillRect(0, 0, width, height);```

4.	Ripristina la trasparenza normale:

     ```g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));```

Utilizzo tipico nel GameState:

```java
@Override
public TransitionEffect getEnterTransition() {
    return new FadeInEffect(1.5f); // durata 1.5 secondi
}
```

Il motore chiamerà update() e render() fino a quando isComplete() restituirà true. FadeOutEffect funziona in modo analogo, partendo però da un rettangolo trasparente fino ad arrivare a un rettangolo completamente nero.

## 3. UIImage: immagini statiche, animate e mobili

La classe UIImage rappresenta un elemento grafico dell’interfaccia utente (UI) del motore RetroEdge. Può essere usata per disegnare:
- immagini statiche,
- immagini animate (sequenze di frame),
- immagini in movimento da un punto a un altro.

Estende la classe UIElement, ed è quindi integrabile nella UI come componente autonomo.

Attributi principali:

| Attributo	| Tipo	| Descrizione |
|-----------|-------|-------------|
| BufferedImage[] images	| array di immagini	| Contiene i frame da visualizzare (anche uno solo per immagini statiche). |
| float elapsedTime	| temporizzazione	| Tempo trascorso dall’ultimo cambio di frame. |
| float frameDuration	| secondi	| Durata di ogni frame in una sequenza animata. |
| int currentFrame	| indice	| Frame attualmente visualizzato. |
| boolean looping	| flag	| Indica se l’animazione deve ripartire alla fine. |
| boolean animating	| flag	| Indica se è in corso un’animazione. |
| Runnable onAnimationEnd	| callback	| Azione da eseguire al termine dell’animazione. |
| boolean moving	| flag	| Indica se l’immagine si sta spostando. |
| float endX, endY	| coordinate	| Destinazione dell’immagine in movimento. |
| float xSpeed, ySpeed	| velocità	| Velocità di movimento lungo gli assi. |
| Runnable onMotionEnd	| callback	| Azione da eseguire quando il movimento termina. |

Costruttori:

| Costruttore	| Descrizione |
|-------------|-------------|
| UIImage(int x, int y, String fileName, int layer)	| Crea un’immagine statica caricando il file PNG. |
| UIImage(int x, int y, Spritesheet sheet, float frameDuration, int layer)	| Crea un’immagine animata usando i frame estratti da uno Spritesheet. |

Metodi principali:

| Metodo	| Descrizione |
|---------|-------------|
| setDestination(int x, int y, float speed)	| Imposta un punto finale e una velocità per avviare il movimento verso quella posizione. |
| startAnimation()	| Riavvia l’animazione dal primo frame. |
| setLooping(boolean looping)	| Imposta se l’animazione deve ripetersi. |
| setOnAnimationEnd(Runnable r)	| Imposta un’azione da eseguire alla fine dell’animazione. |
| setOnMotionEnd(Runnable r)	| Imposta un’azione da eseguire alla fine del movimento. |


Il metodo ```update(float deltaTime)```  aggiorna lo stato dell’immagine:

1.	Animazione:
    - Aggiunge deltaTime a elapsedTime.
    - Quando elapsedTime >= frameDuration, passa al frame successivo.
    - Quando si raggiunge l’ultimo frame, se looping == true riparte, altrimenti, ferma l’animazione e chiama onAnimationEnd() (se definito).
2.	Movimento:
    - Aggiorna la posizione x e y in base alle velocità xSpeed e ySpeed.
    - Se viene raggiunta la destinazione, ferma il movimento e chiama onMotionEnd() (se definito).

Il metodo ```render(Graphics2D g)``` disegna l’immagine corrente nel punto (x, y):

```g.drawImage(images[currentFrame], (int) x, (int) y, null);```

Il metodo viene eseguito solo se l’immagine è visible == true.

### 3.1 Esempi d’uso

Immagine statica:

```java
UIImage bg = new UIImage(0, 0, 1, "/ui/background.png");
bg.show();
```

Immagine animata:

```java
UISpritesheet fire = new UISpritesheet("/title/fire.png", 30, 30);
UIImage flame = new UIImage(200, 300, fire, 0.1f, 1);
flame.setLooping(true);
flame.startAnimation();
flame.show();
```

Immagine in movimento:

```java
UIImage sword = new UIImage(195, -256, 3, "/title/logo.png");
logo.setDestination(100, 100, 3.0f);
logo.show();
```

## 4. Impostare lo Stato Iniziale del Gioco

Una volta creato lo stato (ad esempio DemoState), è necessario configurare il motore in modo che venga avviato al lancio del gioco. Per farlo, bisogna modificare il metodo init() nella classe GameEngine, che estende Engine.

Codice da aggiornare:

```java
@Override
protected void init() {
    this.setNextState(new DemoState(this));
}
```

Nota
- Lo stato impostato con setNextState(...) verrà attivato dopo la fase di inizializzazione dell’engine.
- Il motore applicherà eventuali transizioni in ingresso definite dallo stato (getEnterTransition()), se presenti.

## 5. Esempio: Stato DemoState (immagine fissa, animata, in movimento)

La seguente classe rappresenta uno stato di gioco dimostrativo che include:
- un’immagine statica come sfondo,
- un’immagine in movimento (titolo),
- un’immagine animata (una stella che brilla ciclicamente in posizioni differenti),
- la gestione dell’input per il passaggio allo stato successivo,
- l’aggiunta dei sistemi minimi necessari (input, rendering, UI).

Codice completo:

```java
public class DemoState extends GameState {

	private int shineCount = 0;

	public DemoState(Engine engine) {
		super(engine);
	}

	@Override
	public void init() {

		// ENTITÀ UI per raggruppare gli elementi grafici
		Entity intro = new Entity(EntityType.UI, 1);

		// 1. IMMAGINE STATICA: sfondo
		UIImage background = new UIImage(0, 0, 0, "/title/background.png");
		background.show();

		// 2. IMMAGINE IN MOVIMENTO: titolo che scende dall'alto
		UIImage title = new UIImage(160, -100, 1, "/title/title.png");
		title.setDestination(160, 100, 5.0f);
		title.show();

		// 3. IMMAGINE ANIMATA: stella che brilla ciclicamente
		UISpritesheet shineSpritesheet = new UISpritesheet("/title/shining_star.png", 30, 30);
		int[] shineX = { 320, 340, 360 };
		int[] shineY = { 120, 130, 125 };
		
		UIImage shine = new UIImage(shineX[shineCount], shineY[shineCount], 2, shineSpritesheet, 0.1f);
		shine.show();
		shine.setLooping(false);
		shine.setOnAnimationEnd(() -> {
			shineCount = (shineCount + 1) % shineX.length;
			shine.setX(shineX[shineCount]);
			shine.setY(shineY[shineCount]);
			shine.startAnimation();
		});
		shine.startAnimation();

		// GRUPPO UI: contiene tutti gli elementi grafici
		UIGroup introGroup = new UIGroup(0, 0, 1);
		introGroup.addChild(background);
		introGroup.addChild(title);
		introGroup.addChild(shine);

		// COMPONENTE UI associato all'entità
		UIComponent introComponent = new UIComponent(intro, introGroup);
		intro.addComponent(introComponent);
		add(intro);

		// SISTEMA DI INPUT: Enter per passare allo stato di gioco
		InputSystem inputSystem = new InputSystem(this.engine);
		inputSystem.bindCustomAction(InputAction.START, KeyEvent.VK_ENTER, deltaTime -> {
			PlayState state = new PlayState(engine);
			engine.setNextState(state);
		});
		inputSystem.addDebouncedAction(InputAction.START);

		// SISTEMA DI DEBUG: tasto 0 per attivare/disattivare la modalità debug
		inputSystem.bindCustomAction(InputAction.DEBUG, KeyEvent.VK_0, deltaTime -> {
			if (engine.isDebug()) {
				engine.setDebug(false);
			} else {
				GamePanel.resetDebug();
				engine.setDebug(true);
			}
		});
		inputSystem.addDebouncedAction(InputAction.DEBUG);
		add(inputSystem);

		// SISTEMI RENDERING E UI
		add(new RenderingSystem(this.engine, null));
		add(new UISystem(this.engine, null));
	}

	@Override
	public void cleanup() {
		// Pulizia risorse se necessaria
	}

	@Override
	public TransitionEffect getExitTransition() {
		return new FadeOutEffect(0.5f); // Breve dissolvenza in uscita
	}
}
```

Risorse necessarie:

| Percorso	| Tipo	| Descrizione |
|-----------|-------|-------------|
| /title/background.png	| Immagine statica	| Sfondo a schermo intero |
| /title/title.png	| Immagine statica	| Logo o titolo del gioco |
| /title/shining_star.png	| Spritesheet | Stella che brilla in animazione |

Funzionalità dimostrate:

| Componente	| Funzione |
|-------------|----------|
| UIImage	| Visualizza immagini statiche o animate |
| setDestination(...)	| Anima il movimento di un’immagine |
| setOnAnimationEnd(...)	| Gestisce eventi al termine di un ciclo |
| UIGroup	| Raggruppa elementi della UI |
| InputSystem	| Ascolta e gestisce eventi da tastiera |
| FadeOutEffect	| Transizione in uscita |
