package fr.uge.items;

import java.util.Objects;

public record Sword(ItemType type, int dmg, int stamina, int width, int height, int durability) implements Item {
  public Sword {
    Objects.requireNonNull(type);
    if (dmg < 0) throw new IllegalArgumentException("Damage can't be negative");
    if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid size");
  }

  @Override public String name() { return type.toString(); }
  @Override public int attackValue() { return dmg; }
  @Override public int defendValue() { return 0; }

	@Override
	public int staminaCost() {
		return stamina;
	}
	
	public int getDamage() {
		return dmg;
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
  public Item decreaseDurability() {
      if (durability <= 0) return this; 
      return new Sword(type, dmg, stamina, width, height, durability - 1);
  }

  @Override public int durability() { return durability; }

	@Override
	public int price() {
		// TODO Auto-generated method stub
		return dmg;
	}
}
