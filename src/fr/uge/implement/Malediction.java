package fr.uge.implement;

import java.util.Objects;

public record Malediction(String name, boolean[][] shape) implements Item {

	public Malediction {
		Objects.requireNonNull(name);
		Objects.requireNonNull(shape);
	}

	/* ===================== FABRIQUES ===================== */

	public static Malediction carre() {
		return new Malediction("Malédiction (Carré)", new boolean[][] { { true, true }, { true, true } });
	}

	public static Malediction formeS() {
		return new Malediction("Malédiction (S)", new boolean[][] { { false, true, true }, { true, true, false } });
	}

	/* ===================== FORME ===================== */

	public boolean occupies(int dx, int dy) {
		return shape[dy][dx];
	}

	/* ===================== DIMENSIONS ===================== */

	@Override
	public int width() {
		return shape[0].length;
	}

	@Override
	public int height() {
		return shape.length;
	}

	/* ===================== COMPORTEMENT ===================== */

	@Override
	public Item rotate() {
		return this; // ❌ rotation interdite
	}

	@Override
	public boolean isRotated() {
		return false;
	}

	@Override
	public boolean isSellable() {
		return false;
	}

	@Override
	public int price() {
		return 0;
	}

	@Override
	public boolean isStackable() {
		return false;
	}

	@Override
	public int durability() {
		return -1; // indestructible
	}

	@Override
	public Item decreaseDurability() {
		return this;
	}

	/* ===================== STATS ===================== */

	@Override
	public int attackValue() {
		return 0;
	}

	@Override
	public int staminaCost() {
		return 0;
	}

	@Override
	public int defendValue() {
		return 0;
	}

	@Override
	public int staminaRegen() {
		return 0;
	}

	@Override
	public int healthRegen() {
		return 0;
	}
	
	@Override
	public boolean isMalediction() {
	    return true;
	}
}
