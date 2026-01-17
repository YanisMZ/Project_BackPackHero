package fr.uge.items;

import java.util.Objects;

public record Gold(String name, int quantity) implements Item {

	public Gold {
		Objects.requireNonNull(name);

	}

	@Override
	public boolean isStackable() {
		return true;
	}

	@Override
	public int quantity() {
		return quantity;
	}

	@Override
	public Item addQuantity(int amount) {
		return new Gold(name, quantity + amount);
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
	}

	@Override
	public int width() {
		return 1;
	}

	@Override
	public int height() {
		return 1;
	}

	@Override
	public Item rotate() {
		return this;
	}

	@Override
	public boolean isRotated() {
		return false;
	}

	@Override
	public boolean isSellable() {
		return false;
	}

	public int price() {
		return 0;
	}
}
