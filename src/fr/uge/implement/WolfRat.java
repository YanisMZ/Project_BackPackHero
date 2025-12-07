package fr.uge.implement;

public record WolfRat(int hp, int protection) implements Enemy {

  public WolfRat() {
    this(25, 0);
  }

  @Override
  public boolean isAlive() {
    return hp > 0;
  }

  @Override
  public Enemy takeDamage(int dmg) {
    int finalDmg = Math.max(0, dmg - protection);
    return new WolfRat(hp - finalDmg, protection);
  }

  @Override
  public Enemy defend() {
    return new WolfRat(hp, protection + 2);
  }

  @Override
  public String name() {
    // TODO Auto-generated method stub
    return "WolfRat";
  }

	@Override
	public int attackDamage() {
		// TODO Auto-generated method stub
		return 8;
	}

}
