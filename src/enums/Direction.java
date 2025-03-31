package enums;

public enum Direction {
    UP(0),
    DOWN(1),
    LEFT(2),
    RIGHT(3);

    private final int animationIndex;

    Direction(int animationIndex) {
        this.animationIndex = animationIndex;
    }

    public int getDirectionIndex() {
        return animationIndex;
    }
}