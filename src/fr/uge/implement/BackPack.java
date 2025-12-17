package fr.uge.implement;

import java.util.Objects;

/**
 * Backpack grid inventory (Diablo / RE4 style)
 * The backpack grows by unlocking individual cells.
 */
public class BackPack {

    private final int width;
    private final int height;

    private final Item[][] grid;
    private final boolean[][] unlocked;

    /**
     * width/height = taille MAX du sac
     */
    public BackPack(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException();

        this.width = width;
        this.height = height;
        this.grid = new Item[height][width];
        this.unlocked = new boolean[height][width];
    }

    /* ===================== ACCESSORS ===================== */

    public int width() { return width; }
    public int height() { return height; }
    public Item[][] grid() { return grid; }

    public boolean isUnlocked(int x, int y) {
        return unlocked[y][x];
    }

    /* ===================== UNLOCK ===================== */

    /** Unlock a single cell */
    public void unlockCell(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw new IllegalArgumentException();
        unlocked[y][x] = true;
    }

    /** Unlock several cells at once */
    public void unlockCells(int[][] cells) {
        for (int[] c : cells) {
            unlockCell(c[0], c[1]);
        }
    }

    /* ===================== PLACEMENT ===================== */

    public boolean canPlace(Item item, int x, int y) {
        Objects.requireNonNull(item);

        for (int dy = 0; dy < item.height(); dy++) {
            for (int dx = 0; dx < item.width(); dx++) {
                int gx = x + dx;
                int gy = y + dy;

                if (gx < 0 || gy < 0 || gx >= width || gy >= height)
                    return false;

                if (!unlocked[gy][gx])
                    return false;

                if (grid[gy][gx] != null)
                    return false;
            }
        }
        return true;
    }

    public boolean place(Item item, int x, int y) {
        if (!canPlace(item, x, y))
            return false;

        for (int dy = 0; dy < item.height(); dy++) {
            for (int dx = 0; dx < item.width(); dx++) {
                grid[y + dy][x + dx] = item;
            }
        }
        return true;
    }

    /**
     * Try to add an item anywhere in the backpack.
     * Tries normal orientation first, then rotated.
     */
    public boolean autoAdd(Item item) {
      Objects.requireNonNull(item);

      // stack
      if (item.isStackable()) {
          for (int y = 0; y < height; y++) {
              for (int x = 0; x < width; x++) {
                  Item cell = grid[y][x];
                  if (cell != null
                          && cell.isStackable()
                          && cell.name().equals(item.name())) {

                      grid[y][x] = cell.addQuantity(item.quantity());
                      return true;
                  }
              }
          }
      }

     
      for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {

              if (canPlace(item, x, y))
                  return place(item, x, y);

              Item rotated = item.rotate();
              if (canPlace(rotated, x, y))
                  return place(rotated, x, y);
          }
      }
      return false;
  }


    /**
     * Try to add an item at a specific position.
     * Accepts rotation implicitly.
     */
    public boolean addAt(Item item, int x, int y) {
        Objects.requireNonNull(item);

        if (canPlace(item, x, y))
            return place(item, x, y);

        Item rotated = item.rotate();
        if (canPlace(rotated, x, y))
            return place(rotated, x, y);

        return false;
    }

    /* ===================== REMOVE / MOVE ===================== */

    public boolean remove(Item item) {
        Objects.requireNonNull(item);

        boolean removed = false;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[y][x] == item) {
                    grid[y][x] = null;
                    removed = true;
                }
            }
        }
        return removed;
    }

    /**
     * Move an item to a new position.
     * Keeps orientation if possible, otherwise tries rotated.
     */
    public boolean move(Item item, int newX, int newY) {
        Objects.requireNonNull(item);

        remove(item);

        if (place(item, newX, newY))
            return true;

        Item rotated = item.rotate();
        if (place(rotated, newX, newY))
            return true;

        autoAdd(item);
        return false;
    }

    /* ===================== QUERY ===================== */

    public boolean contains(Item item) {
        Objects.requireNonNull(item);

        for (Item[] row : grid) {
            for (Item cell : row) {
                if (cell == item)
                    return true;
            }
        }
        return false;
    }
}
