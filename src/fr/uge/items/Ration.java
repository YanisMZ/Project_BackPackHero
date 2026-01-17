package fr.uge.items;

import java.util.Objects;

public record Ration(String name, int width, int height, int durability) implements Item {

	public Ration {
		Objects.requireNonNull(name);
	}

	// Constructeur pratique pour une nouvelle ration
	public Ration(String name, int width, int height) {
		this(name, width, height, 1);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int attackValue() {
		return 0;
	}

	@Override
	public int defendValue() {
		return 0;
	}

	@Override
	public int staminaCost() {
		return 0;
	} // Utilisation gratuite

	@Override
	public int staminaRegen() {
		return 1;
	}

	@Override
	public int durability() {
		return durability;
	}

	@Override
	public Item decreaseDurability() {
		return new Ration(name, width, height, 0); // Devient cassé immédiatement
	}

	@Override
	public Item rotate() {
		return new RotatableItem(this, true);
	}

	@Override
	public boolean isRotated() {
		return false;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public int price() {

		return 10;
	}
}