package fr.uge.implement;

import java.util.Objects;

public record Sword(String name, int dmg) implements Item {
  public Sword {
    Objects.requireNonNull(name);
    if (dmg < 0) {
      throw new IllegalArgumentException("Dammage can't be less than 0");
    }
  }

  public String name() {
    return this.name;
  }

  @Override
  public int attackValue() {
    // TODO Auto-generated method stub
    return dmg;
  }

  @Override
  public int defendValue() {
    // TODO Auto-generated method stub
    return 0;
  }
}
