package enums;

public enum Action {
	IDLE(0),
	WALK(1),
	JUMP(2),
	FALL(3),
	ATTACK(4),
	THROW(5),
	ATTACK_JUMP(6),;
	
    private final int actionIndex;

    Action(int actionIndex) {
        this.actionIndex = actionIndex;
    }

    public int getActionIndex() {
        return actionIndex;
    }
}
