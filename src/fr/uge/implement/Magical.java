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
}
