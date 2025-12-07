package fr.uge.implement;

public record Mana(String name, int amount) implements Item {

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
