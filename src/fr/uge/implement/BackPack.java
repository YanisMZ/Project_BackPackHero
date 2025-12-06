package fr.uge.implement;

import java.util.Objects;

public class BackPack {
  private final Item[] grid = new Item[15]; // 3×5
  private int count = 0;

  public int add(Item item) {
      Objects.requireNonNull(item);

      if (count >= 15) {
          return 0; // sac plein
      }

      for (int i = 0; i < grid.length; i++) {
          if (grid[i] == null) {
              grid[i] = item;
              count++;
              return 1;
          }
      }
      return 0;
  }

  public boolean remove(Item item) {
      Objects.requireNonNull(item);
      for (int i = 0; i < grid.length; i++) {
          if (grid[i] == item) {
              grid[i] = null;
              count--;
              return true;
          }
      }
      return false;
  }

  public Item[] grid() {
      return grid;
  }


  public boolean move(int fromIndex, int toIndex) {
      if (fromIndex < 0 || fromIndex >= 15) return false;
      if (toIndex < 0 || toIndex >= 15) return false;

      Item obj = grid[fromIndex];
      if (obj == null) return false;      // pas d’objet à déplacer
      if (grid[toIndex] != null) return false; // case d’arrivée occupée

      // déplacement
      grid[toIndex] = obj;
      grid[fromIndex] = null;
      return true;
  }
}

