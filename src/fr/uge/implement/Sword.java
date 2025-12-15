package fr.uge.implement;

import java.util.Objects;

public record Sword(String name, int dmg,int stamina, int width, int height) implements Item {
  public Sword {
    Objects.requireNonNull(name);
    if (dmg < 0) throw new IllegalArgumentException("Damage can't be negative");
    if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid size");
  }

  @Override public String name() { return name; }
  @Override public int attackValue() { return dmg; }
  @Override public int defendValue() { return 0; }

	@Override
	public int staminaCost() {
		return stamina;
	}
}
