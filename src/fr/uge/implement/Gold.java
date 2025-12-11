package fr.uge.implement;

import java.util.Objects;

public record Gold(String name) implements Item {
	public Gold{
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

}
