package fr.uge.backpack;

import java.util.HashSet;

import java.util.Objects;
import java.util.Set;

import fr.uge.items.Gold;
import fr.uge.items.Item;

/**
 * Class for the bag of hero.
 * It is a grid where we put items.
 */
public class BackPack {

	private final int width;
	private final int height;
	private final Item[][] grid;
	private final boolean[][] unlocked;

	public BackPack(int width, int height) {
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException();

		this.width = width;
		this.height = height;
		this.grid = new Item[height][width];
		this.unlocked = new boolean[height][width];
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public Item[][] grid() {
		return grid;
	}

	public boolean isUnlocked(int x, int y) {
		return unlocked[y][x];
	}

	public void unlockCell(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height)
			throw new IllegalArgumentException();
		unlocked[y][x] = true;
	}

	public void unlockCells(int[][] cells) {
		for (int[] c : cells)
			unlockCell(c[0], c[1]);
	}
	/**
	 * return True if we can put an item at x, y.
	 * Verify if cell is unlocked and empty.
	 */
	public boolean canPlace(Item item, int x, int y) {
		Objects.requireNonNull(item);

		for (int dy = 0; dy < item.height(); dy++) {
			for (int dx = 0; dx < item.width(); dx++) {

				if (!item.occupies(dx, dy))
					continue;

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
	
	/**
	 * Return list of items that block the place.
	 */
	public Set<Item> blockingItems(Item item, int x, int y) {
		Objects.requireNonNull(item);
		Set<Item> result = new HashSet<>();

		for (int dy = 0; dy < item.height(); dy++) {
			for (int dx = 0; dx < item.width(); dx++) {

				if (!item.occupies(dx, dy))
					continue;

				int gx = x + dx;
				int gy = y + dy;

				if (gx < 0 || gy < 0 || gx >= width || gy >= height)
					continue;

				Item existing = grid[gy][gx];
				if (existing != null) {
					result.add(existing);
				}
			}
		}
		return result;
	}
	
	public boolean canForcePlace(Item item, int x, int y) {
		Objects.requireNonNull(item);

		for (int dy = 0; dy < item.height(); dy++) {
			for (int dx = 0; dx < item.width(); dx++) {

				if (!item.occupies(dx, dy))
					continue;

				int gx = x + dx;
				int gy = y + dy;

				if (gx < 0 || gy < 0 || gx >= width || gy >= height)
					return false;

				if (!unlocked[gy][gx])
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

				if (!item.occupies(dx, dy))
					continue;
				grid[y + dy][x + dx] = item;
			}
		}
		return true;
	}
	
	/**
	 * Try to add automatically in the bag an selected item.
	 * * @param item Item to add.
	 * @return True if added, false if bag is full.
	 */
	public boolean autoAdd(Item item) {
		Objects.requireNonNull(item);

		// Fusionner avec un stack existant
		if (item.isStackable()) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					Item cell = grid[y][x];
					if (cell != null && cell.isStackable() && cell.name().equals(item.name())) {
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

	public boolean remove(Item item) {
		Objects.requireNonNull(item);
		boolean removed = false;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				if (grid[y][x] == item) {
					grid[y][x] = null;
					removed = true;
				}
		return removed;
	}

	public int getQuantity(String itemName) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				Item item = grid[y][x];
				if (item != null && item.isStackable() && item.name().equals(itemName))
					return item.quantity();
			}
		return 0;
	}

	public void addQuantity(String itemName, int amount) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Item item = grid[y][x];
				if (item != null && item.isStackable() && item.name().equals(itemName)) {
					grid[y][x] = item.addQuantity(amount);
					return;
				}
			}
		}

		autoAdd(new Gold(itemName, amount));
	}

	public void removeQuantity(String itemName, int amount) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				Item item = grid[y][x];
				if (item != null && item.isStackable() && item.name().equals(itemName)) {
					if (item.quantity() < amount)
						throw new IllegalStateException("Pas assez de " + itemName + " !");
					grid[y][x] = item.addQuantity(-amount);
					return;
				}
			}
		throw new IllegalStateException("Pas assez de " + itemName + " !");
	}

	public void updateItem(Item oldItem, Item newItem) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (grid[y][x] == oldItem) {
					grid[y][x] = newItem;
				}
			}
		}
	}

	public void forcePlace(Item item, int x, int y) {
		Objects.requireNonNull(item);

		if (!canForcePlace(item, x, y))
			throw new IllegalStateException("Placement impossible");

		if (!blockingItems(item, x, y).isEmpty())
			throw new IllegalStateException("Objets bloquants non résolus");

		for (int dy = 0; dy < item.height(); dy++) {
			for (int dx = 0; dx < item.width(); dx++) {

				if (!item.occupies(dx, dy))
					continue;
				grid[y + dy][x + dx] = item;
			}
		}
	}

	public boolean contains(Item item) {
		if (item == null)
			return false;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (grid[y][x] == item) {
					return true;
				}
			}
		}
		return false;
	}

}
