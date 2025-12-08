package fr.uge.implement;

public record Shield(String name, int hp) implements Item {

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

}
