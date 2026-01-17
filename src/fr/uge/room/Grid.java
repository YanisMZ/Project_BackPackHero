package fr.uge.room;

import java.util.Random;

import fr.uge.items.Item;
import fr.uge.items.ItemFactory;

/**
 * Represents a grid used to store items.
 * Items can occupy multiple cells depending on their size.
 */
public class Grid {

	private final Item[][] grid;
	private final int rows;
	private final int cols;

	/**
	 * Creates a grid with the given size.
	 *
	 * @param rows number of rows
	 * @param cols number of columns
	 */
	public Grid(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.grid = new Item[rows][cols];
	}

	/**
	 * @return the grid of items
	 */
	public Item[][] getGrid() {
		return grid;
	}

	/**
	 * @return number of rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @return number of columns
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * Clears the grid.
	 * All cells become empty.
	 */
	public void clear() {
		for (int y = 0; y < rows; y++)
			for (int x = 0; x < cols; x++)
				grid[y][x] = null;
	}

	/**
	 * Tries to place an item in the grid.
	 * Stackable items are stacked if possible.
	 *
	 * @param item item to place
	 * @return true if the item was placed
	 */
	public boolean placeItem(Item item) {

		if (item.isStackable()) {
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < cols; x++) {
					Item cell = grid[y][x];
					if (cell != null && cell.isStackable()
							&& cell.name().equals(item.name())) {
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

	/**
	 * Checks if an item can be placed at the given position.
	 *
	 * @param item item to place
	 * @param x    column index
	 * @param y    row index
	 * @return true if the item can be placed
	 */
	public boolean canPlace(Item item, int x, int y) {
		if (x + item.width() > cols || y + item.height() > rows)
			return false;

		for (int dy = 0; dy < item.height(); dy++)
			for (int dx = 0; dx < item.width(); dx++)
				if (grid[y + dy][x + dx] != null)
					return false;

		return true;
	}

	/**
	 * Removes an item from the grid.
	 *
	 * @param item item to remove
	 */
	public void removeItem(Item item) {
		for (int y = 0; y < rows; y++)
			for (int x = 0; x < cols; x++)
				if (grid[y][x] == item)
					grid[y][x] = null;
	}

	/**
	 * @return true if the grid is empty
	 */
	public boolean isEmpty() {
		for (int y = 0; y < rows; y++)
			for (int x = 0; x < cols; x++)
				if (grid[y][x] != null)
					return false;
		return true;
	}

	/**
	 * Generates random items and places them in the grid.
	 *
	 * @param minItems minimum number of items
	 * @param maxItems maximum number of items
	 */
	public void generateRandomItems(int minItems, int maxItems) {
		clear();
		Random random = new Random();
		int numItems = minItems + random.nextInt(maxItems - minItems + 1);

		for (int i = 0; i < numItems; i++)
			placeItem(ItemFactory.randomItem());
	}

	/**
	 * Generates random merchant items and places them in the grid.
	 *
	 * @param minItems minimum number of items
	 * @param maxItems maximum number of items
	 */
	public void generateRandomItemsMarchant(int minItems, int maxItems) {
		clear();
		Random random = new Random();
		int numItems = minItems + random.nextInt(maxItems - minItems + 1);

		for (int i = 0; i < numItems; i++)
			placeItem(ItemFactory.randomItemMarchant());
	}
}
