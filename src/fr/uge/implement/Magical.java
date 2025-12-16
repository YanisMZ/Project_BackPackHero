package fr.uge.implement;

import java.util.Objects;

public record Magical(String name) implements Item {
  public Magical {
    Objects.requireNonNull(name);
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
}
