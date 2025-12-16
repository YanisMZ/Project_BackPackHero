package fr.uge.implement;
public final class RotatableItem implements Item {

    private final Item base;
    private final boolean rotated;

    public RotatableItem(Item base) {
        this(base, false);
    }

    RotatableItem(Item base, boolean rotated) {
        this.base = base;
        this.rotated = rotated;
    }

    @Override
    public Item rotate() {
        return new RotatableItem(base, !rotated);
    }

    @Override
    public boolean isRotated() {
        return rotated;
    }

    @Override public String name() { return base.name(); }
    @Override public int attackValue() { return base.attackValue(); }
    @Override public int staminaCost() { return base.staminaCost(); }
    @Override public int defendValue() { return base.defendValue(); }

    @Override
    public int width() {
        return rotated ? base.height() : base.width();
    }

    @Override
    public int height() {
        return rotated ? base.width() : base.height();
    }
}
