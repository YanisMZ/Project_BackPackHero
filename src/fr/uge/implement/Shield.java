package fr.uge.implement;

public record Shield(String name, int hp,int width, int height) implements Item {

  @Override
  public int attackValue() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int defendValue() {
    // TODO Auto-generated method stub
    return 5;
  }

	@Override
	public int staminaCost() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
  public Item rotate() {
      return new RotatableItem(this, true);
  }

  @Override
  public boolean isRotated() {
      return false;
  }

}
