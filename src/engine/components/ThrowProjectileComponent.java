package engine.components;

import engine.Component;
import engine.Entity;

public class ThrowProjectileComponent extends Component {
    private Entity projectile;          // Riferimento al proiettile
    private float throwTimer;           // Timer per il lancio
    private float timeToThrow;          // Tempo minimo per un nuovo lancio
    private float throwingTime;         // Durata totale dell'animazione di lancio
    private boolean throwingProjectile; // Stato del lancio
    private boolean projectileLaunched; // Stato del lancio

    public ThrowProjectileComponent(Entity entity, float timeToThrow, float throwingTime, Entity projectile) {
        super(entity);
        this.projectile = projectile;
        this.timeToThrow = timeToThrow;
        this.throwingTime = throwingTime;
        this.throwTimer = 0;
        this.throwingProjectile = false;
        this.projectileLaunched = false;
    }

    public Entity getProjectile() {
        return projectile;
    }

    public void setProjectile(Entity projectile) {
        this.projectile = projectile;
    }

    public void update(float deltaTime) {
        if (throwingProjectile) {
            throwTimer += deltaTime;
            if (throwTimer >= throwingTime) {
                throwingProjectile = false; // Fine dell'animazione di lancio
                projectileLaunched = false;
            }
        } else {
            throwTimer += deltaTime;
        }
    }

    /**
     * Verifica se il proiettile può essere lanciato.
     *
     * @return `true` se il proiettile può essere lanciato
     */
    public boolean canThrowProjectile() {
        return throwTimer >= timeToThrow && projectile != null && !projectile.isAlive();
    }

    /**
     * Avvia il lancio del proiettile.
     *
     * @param direction La direzione del lancio
     */
    public void throwProjectile() {
        if (canThrowProjectile()) {
            this.throwingProjectile = true;
            this.projectileLaunched = false;
            this.throwTimer = 0; // Resetta il timer per il lancio
        }
    }

    /**
     * Controlla se l'entity sta lanciando un proiettile.
     *
     * @return `true` se l'animazione di lancio è attiva
     */
    public boolean isThrowing() {
        return throwingProjectile;
    }

    public void setProjectileLaunched(boolean projectileLaunched) {
		this.projectileLaunched = projectileLaunched;
	}

	public boolean isProjectileLaunched() {
		return projectileLaunched;
	}

	public float getThrowingTime() {
        return throwingTime;
    }

    public float getThrowTimer() {
        return throwTimer;
    }

    /**
     * Calcola il frame corrente basato sul timer e sulla durata dell'animazione.
     *
     * @param totalFrames Numero totale di frame nell'animazione
     * @return Il frame corrente (intero)
     */
    public int getCurrentFrame(int totalFrames) {
        float animationTimePerFrame = throwingTime / totalFrames;
        return Math.min(totalFrames - 1, (int) (throwTimer / animationTimePerFrame));
    }

    /**
     * Resetta il timer di lancio e lo stato di animazione.
     */
    public void resetTimer() {
        throwTimer = 0;
        throwingProjectile = false;
    }
}