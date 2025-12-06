package fr.uge.implement;

public class Hero {
  private int hp;
  private int mana;

  public Hero(int hp, int mana) {
    this.hp = hp;
    this.mana = mana;
  }

  public int hp() {
    return hp;
  }

  public int mana() {
    return mana;
  }

  public void takeDamage(int amount) {
    hp = Math.max(0, hp - amount);
  }

  public void restoreMana(int amount) {
    mana += amount;
  }

  @Override
  public String toString() {
    return "Hero(hp=" + hp + ", mana=" + mana + ")";
  }
}
