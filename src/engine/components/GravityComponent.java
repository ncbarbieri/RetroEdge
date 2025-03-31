package engine.components;

import engine.Component;
import engine.Entity;

public class GravityComponent extends Component {
    private float airSpeed;
    private float gravity;
    private float jumpSpeed;
    private float fallSpeedAfterCollision;
    private boolean inAir = false;

    public GravityComponent(Entity entity, float gravity, float jumpSpeed, float fallSpeedAfterCollision) {
        super(entity);
        this.airSpeed = 0f;
        this.gravity = gravity;
        this.jumpSpeed = jumpSpeed;
        this.fallSpeedAfterCollision = fallSpeedAfterCollision;
    }

    // Metodo per aggiornare la velocità in aria
    public void updateAirSpeed(float deltaTime) {
        if (inAir) {
            airSpeed += gravity * deltaTime;
        } else {
            airSpeed = 0; // Reset della velocità se a terra
        }
    }

    // Imposta se l'entità è a terra o in aria
    public void setOnGround(boolean onGround) {
        if (onGround) {
            inAir = false;
            airSpeed = 0; // Reset airSpeed quando si tocca il terreno
        } else {
            inAir = true;
        }
    }

    public float getAirSpeed() {
        return airSpeed;
    }

    // Metodo per far saltare l'entità
    public void jump() {
        if (!inAir) {
            airSpeed = jumpSpeed;
            inAir = true;
        }
    }

    // Metodo per resettare l'airSpeed (quando si tocca il terreno)
    public void resetAirSpeed() {
        airSpeed = 0;
    }

    public void setFallSpeedAfterCollision() {
        airSpeed = fallSpeedAfterCollision;
    }

    public boolean isInAir() {
        return inAir;
    }
}