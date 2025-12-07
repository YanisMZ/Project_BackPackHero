package fr.uge.implement;

import java.util.Objects;

public record Mana(String name, int amount) implements Item {
	 public Mana {
	    Objects.requireNonNull(name);
	    if (amount < 0) {
	      throw new IllegalArgumentException("amount can't be less than 0");
	    }
	  }

  @Override
  public int attackValue() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int defendValue() {
    // TODO Auto-generated method stub
    return 0;
  }

}
