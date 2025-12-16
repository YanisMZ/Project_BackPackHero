package fr.uge.implement;

public interface Item {
  String name();
  int attackValue();
  int staminaCost();
  int defendValue();

  int width();
  int height();

  
  Item rotate();
  boolean isRotated();
}

