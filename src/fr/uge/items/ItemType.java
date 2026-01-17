package fr.uge.items;

public enum ItemType {
	SWORD("Sword"), HACHETTE("Hachette"), ARC("Arc");

	private final String label;

	ItemType(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}