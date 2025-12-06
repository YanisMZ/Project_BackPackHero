package fr.uge.implement;

public sealed interface Enemy permits SmallWolfRat, WolfRat {
  int hp();

  int protection();

  boolean isAlive();

  Enemy takeDamage(int dmg);

  Enemy defend();

}
