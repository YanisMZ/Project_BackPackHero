package fr.uge.room;

import java.util.Random;

import fr.uge.items.Item;
import fr.uge.items.ItemFactory;

public class Grid {

    private final Item[][] grid;
    private final int rows;
    private final int cols;

    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Item[rows][cols];
    }

    public Item[][] getGrid() {
        return grid;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void clear() {
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                grid[y][x] = null;
    }

    public boolean placeItem(Item item) {

        
        if (item.isStackable()) {
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    Item cell = grid[y][x];
                    if (cell != null && cell.isStackable() && cell.name().equals(item.name())) {
                        grid[y][x] = cell.addQuantity(item.quantity());
                        return true;
                    }
                }
            }
        }

      
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (canPlace(item, x, y)) {
                    for (int dy = 0; dy < item.height(); dy++)
                        for (int dx = 0; dx < item.width(); dx++)
                            grid[y + dy][x + dx] = item;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canPlace(Item item, int x, int y) {
        if (x + item.width() > cols || y + item.height() > rows)
            return false;
        for (int dy = 0; dy < item.height(); dy++)
            for (int dx = 0; dx < item.width(); dx++)
                if (grid[y + dy][x + dx] != null)
                    return false;
        return true;
    }

    public void removeItem(Item item) {
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                if (grid[y][x] == item)
                    grid[y][x] = null;
    }

    public boolean isEmpty() {
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                if (grid[y][x] != null)
                    return false;
        return true;
    }

    public void generateRandomItems(int minItems, int maxItems) {
        clear();
        Random random = new Random();
        int numItems = minItems + random.nextInt(maxItems - minItems + 1);
        for (int i = 0; i < numItems; i++)
            placeItem(ItemFactory.randomItem());
    }
    
    
    public void generateRandomItemsMarchant(int minItems, int maxItems) {
      clear();
      Random random = new Random();
      int numItems = minItems + random.nextInt(maxItems - minItems + 1);
      for (int i = 0; i < numItems; i++)
          placeItem(ItemFactory.randomItemMarchant());
  }
    
    
}
