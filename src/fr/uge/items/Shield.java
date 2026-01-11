package fr.uge.items;

public record Shield(String name, int hp,int width, int height,int durability) implements Item {

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
  
  @Override
  public Item decreaseDurability() {
      if (durability <= 0) return this; 
      return new Shield(name, hp, width, height, durability - 1);
  }

  @Override public int durability() { return durability; }

	@Override
	public int price() {
		// TODO Auto-generated method stub
		return hp;
	}

}
