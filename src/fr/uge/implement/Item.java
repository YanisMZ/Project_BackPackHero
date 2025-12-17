package fr.uge.implement;

public interface Item {
    String name();

    int attackValue();
    int staminaCost();
    int defendValue();

    int width();
    int height();

    Item rotate();
    boolean isRotated();

    // Stack
    default boolean isStackable() {
        return false;
    }

    default int quantity() {
        return 1;
    }

    default Item addQuantity(int amount) {
        throw new UnsupportedOperationException();
    }
}
