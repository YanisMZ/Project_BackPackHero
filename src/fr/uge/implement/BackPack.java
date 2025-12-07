package fr.uge.implement;

import java.util.Objects;

/**
 * A simple backpack that can store up to 15 items in a fixed grid.
 */
public class BackPack {
	private final Item[] grid = new Item[15]; // 3×5
	private int count = 0;

	/**
	 * Adds an item to the first free slot in the backpack.
	 *
	 * @param item the item to add
	 * @return 1 if the item was added, 0 if the backpack is full
	 */
	public int add(Item item) {
		Objects.requireNonNull(item);

		if (count >= 15) {
			return 0; // full
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
	 /**
   * Removes the first occurrence of the given item from the backpack.
   *
   * @param item the item to remove
   * @return true if the item was found and removed, false otherwise
   */
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

	
	 /**
   * Returns the internal grid storing the items.
   * The returned array is the actual storage.
   *
   * @return the grid of items
   */
	public Item[] grid() {
		return grid;
	}

	/**
   * Moves an item from one position to another.
   * The move only succeeds if both indices are valid,
   * the source slot contains an item, and the destination is empty.
   *
   * @param fromIndex the index of the item to move
   * @param toIndex the target index
   * @return true if the item was moved, false otherwise
   */
	public boolean move(int fromIndex, int toIndex) {
		if (fromIndex < 0 || fromIndex >= 15)
			return false;
		if (toIndex < 0 || toIndex >= 15)
			return false;

		Item obj = grid[fromIndex];
		if (obj == null)
			return false; // nothing to move
		if (grid[toIndex] != null)
			return false;

		// moving
		grid[toIndex] = obj;
		grid[fromIndex] = null;
		return true;
	}
}
