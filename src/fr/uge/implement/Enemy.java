package fr.uge.implement;

public sealed interface Enemy permits SmallWolfRat, WolfRat {
  int hp();
  String name();
  int protection();

  boolean isAlive();

  Enemy takeDamage(int dmg);

  Enemy defend();

}
