package fr.uge.items;

import java.util.Objects;

public record RotatableItem(Item base, boolean rotated) implements Item {

	public RotatableItem {
		Objects.requireNonNull(base);
	}

	public RotatableItem(Item base) {
		this(base, false);
	}

	// --- AJOUTS POUR LA DURABILITÉ ---

	@Override
	public int durability() {
		return base.durability();
	}

	@Override
	public boolean isBroken() {
		return base.isBroken();
	}

	@Override
	public Item decreaseDurability() {
		// IMPORTANT : On diminue la durabilité de la base
		// ET on la ré-enveloppe dans un RotatableItem pour garder la rotation !
		return new RotatableItem(base.decreaseDurability(), rotated);
	}

	@Override
	public int staminaRegen() {
		return base.staminaRegen();
	}

	@Override
	public int healthRegen() {
		return base.healthRegen();
	}

	// --- FIN DES AJOUTS ---

	@Override
	public Item rotate() {
		return new RotatableItem(base, !rotated);
	}

	@Override
	public boolean isRotated() {
		return rotated;
	}

	@Override
	public String name() {
		return base.name();
	}

	@Override
	public int attackValue() {
		return base.attackValue();
	}

	@Override
	public int staminaCost() {
		return base.staminaCost();
	}

	@Override
	public int defendValue() {
		return base.defendValue();
	}

	@Override
	public int price() {
		return base.price();
	}

	@Override
	public boolean isSellable() {
		return base.isSellable();
	}

	@Override
	public int width() {
		return rotated ? base.height() : base.width();
	}

	@Override
	public int height() {
		return rotated ? base.width() : base.height();
	}
}