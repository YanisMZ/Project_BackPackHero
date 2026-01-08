package fr.uge.implement;

public record HealingItem(String name, int healAmount, int width, int height, int durability) implements Item {
    
    public HealingItem(String name, int healAmount, int width, int height) {
        this(name, healAmount, width, height, 1);
    }

    @Override public String name() { return name; }
    @Override public int healthRegen() { return healAmount; } // Renvoie la valeur de soin
    @Override public int staminaCost() { return 0; } // Souvent gratuit à utiliser

    @Override
    public Item decreaseDurability() {
        return new HealingItem(name, healAmount, width, height, durability - 1);
    }

    @Override public int attackValue() { return 0; }
    @Override public int defendValue() { return 0; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public Item rotate() { return new RotatableItem(this, true); }
    @Override public boolean isRotated() { return false; }
}