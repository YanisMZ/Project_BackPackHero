package fr.uge.enemy;

public sealed interface Enemy permits SmallWolfRat, WolfRat {
  int hp();
  int maxHp();

  String name();

  int protection();

  boolean isAlive();
  
  int attackDamage();

  Enemy takeDamage(int dmg);

  Enemy defend();

}
