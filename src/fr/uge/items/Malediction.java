package fr.uge.items;

import java.util.Objects;

/**
 * Represents a cursed item (Malediction).
 * A malediction occupies specific cells in the inventory using a shape.
 * It cannot be sold, stacked, rotated, or destroyed.
 */
public record Malediction(String name, boolean[][] shape) implements Item {

	/**
	 * Creates a new Malediction.
	 *
	 * @param name  the name of the malediction
	 * @param shape the shape of the malediction in the inventory
	 */
	public Malediction {
		Objects.requireNonNull(name);
		Objects.requireNonNull(shape);
	}

	/**
	 * Creates a square-shaped malediction (2x2).
	 *
	 * @return a square malediction
	 */
	public static Malediction carre() {
		return new Malediction("Malédiction (Carré)",
				new boolean[][] { { true, true }, { true, true } });
	}

	/**
	 * Creates an S-shaped malediction.
	 *
	 * @return an S-shaped malediction
	 */
	public static Malediction formeS() {
		return new Malediction("Malédiction (S)",
				new boolean[][] { { false, true, true }, { true, true, false } });
	}

	/**
	 * Checks if the malediction occupies the given cell.
	 *
	 * @param dx horizontal offset
	 * @param dy vertical offset
	 * @return true if the cell is occupied
	 */
	@Override
	public boolean occupies(int dx, int dy) {
		return shape[dy][dx];
	}

	/**
	 * @return the width of the malediction shape
	 */
	@Override
	public int width() {
		return shape[0].length;
	}

	/**
	 * @return the height of the malediction shape
	 */
	@Override
	public int height() {
		return shape.length;
	}

	/**
	 * Maledictions cannot be rotated.
	 *
	 * @return this item
	 */
	@Override
	public Item rotate() {
		return this;
	}

	/**
	 * @return false, maledictions are never rotated
	 */
	@Override
	public boolean isRotated() {
		return false;
	}

	/**
	 * @return false, maledictions cannot be sold
	 */
	@Override
	public boolean isSellable() {
		return false;
	}

	/**
	 * @return 0, maledictions have no price
	 */
	@Override
	public int price() {
		return 0;
	}

	/**
	 * @return false, maledictions cannot be stacked
	 */
	@Override
	public boolean isStackable() {
		return false;
	}

	/**
	 * @return -1, maledictions are indestructible
	 */
	@Override
	public int durability() {
		return -1;
	}

	/**
	 * Does nothing because maledictions cannot lose durability.
	 *
	 * @return this item
	 */
	@Override
	public Item decreaseDurability() {
		return this;
	}

	/**
	 * @return 0, no attack value
	 */
	@Override
	public int attackValue() {
		return 0;
	}

	/**
	 * @return 0, no stamina cost
	 */
	@Override
	public int staminaCost() {
		return 0;
	}

	/**
	 * @return 0, no defense value
	 */
	@Override
	public int defendValue() {
		return 0;
	}

	/**
	 * @return 0, no stamina regeneration
	 */
	@Override
	public int staminaRegen() {
		return 0;
	}

	/**
	 * @return 0, no health regeneration
	 */
	@Override
	public int healthRegen() {
		return 0;
	}

	/**
	 * @return true, this item is a malediction
	 */
	@Override
	public boolean isMalediction() {
		return true;
	}
}
