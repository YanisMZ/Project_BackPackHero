package fr.uge.implement;

public record SmallWolfRat(int hp, int protection) implements Enemy {

  public SmallWolfRat() {
    this(15, 0);
  }

  @Override
  public boolean isAlive() {
    return hp > 0;
  }

  @Override
  public Enemy takeDamage(int dmg) {
    int finalDmg = Math.max(0, dmg - protection);
    return new SmallWolfRat(hp - finalDmg, protection);
  }

  @Override
  public Enemy defend() {
    return new SmallWolfRat(hp, protection + 2);
  }

  @Override
  public String name() {
    // TODO Auto-generated method stub
    return "SmallWolfRat";
  }

	@Override
	public int attackDamage() {
		// TODO Auto-generated method stub
		return 4;
	}

}
