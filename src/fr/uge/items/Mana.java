package fr.uge.items;

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

	@Override
	public int width() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int height() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int staminaCost() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Item rotate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRotated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int price() {
		// TODO Auto-generated method stub
		return amount;
	}
	
	

}
