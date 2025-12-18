package fr.uge.implement;

public enum ItemType {
    SWORD("Sword"),
    HACHETTE("Hachette"),
    ARC("Arc");

    private final String label;

    ItemType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}