package fr.uge.implement;

import java.util.Objects;

/**
 * Backpack grid inventory (Diablo / RE4 style)
 */
public class BackPack {

    private final int width;
    private final int height;
    private final Item[][] grid;

    public BackPack(int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException();
        this.width = width;
        this.height = height;
        this.grid = new Item[height][width];
    }

    public int width() { return width; }
    public int height() { return height; }
    public Item[][] grid() { return grid; }

    /** Checks if an item can fit entirely at (x,y) */
    public boolean canPlace(Item item, int x, int y) {
        Objects.requireNonNull(item);
        for (int dy = 0; dy < item.height(); dy++) {
            for (int dx = 0; dx < item.width(); dx++) {
                int gx = x + dx;
                int gy = y + dy;
                if (gx < 0 || gy < 0 || gx >= width || gy >= height) return false;
                if (grid[gy][gx] != null) return false;
            }
        }
        return true;
    }

    /** Places an item if possible, returns true if successful */
    public boolean place(Item item, int x, int y) {
        if (!canPlace(item, x, y)) return false;

        for (int dy = 0; dy < item.height(); dy++) {
            for (int dx = 0; dx < item.width(); dx++) {
                grid[y + dy][x + dx] = item;
            }
        }
        return true;
    }

    /** Finds first free spot and auto-places item */
    public boolean autoAdd(Item item) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (canPlace(item, x, y)) {
                    return place(item, x, y);
                }
            }
        }
        return false;
    }

    /** Removes the given item entirely */
    public boolean remove(Item item) {
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

    /** Moves item to a new position */
    public boolean move(Item item, int newX, int newY) {
        if (item == null) return false;

        // Remove temporarily
        remove(item);

        // Try place at new location
        if (!place(item, newX, newY)) {
            // Failed, revert — must re-place item somewhere valid
            autoAdd(item);
            return false;
        }
        return true;
    }

    /**
     * Adds an item EXACTLY at (x,y).
     * Used when the player manually chooses the placement.
     */
    public boolean addAt(Item item, int x, int y) {
        Objects.requireNonNull(item);

        if (!canPlace(item, x, y))
            return false;

        return place(item, x, y);
    }
    
    
    
    /** Checks if the backpack contains the given item */
    public boolean contains(Item item) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[y][x] == item) return true;
            }
        }
        return false;
    }


	

		
}
