package fr.uge.backpack;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * System to expand the backpack by unlocking cells near the border.
 */
public class BackpackExpansionSystem {

	private final BackPack backpack;
	private int pendingUnlocks = 0;
	private List<Point> availableExpansions = new ArrayList<>();

	public BackpackExpansionSystem(BackPack backpack) {
		this.backpack = backpack;
		updateAvailableExpansions();
	}

	/**
	 * Add points to unlock cells (called when enemy is dead)
	 */
	public void addPendingUnlocks(int count) {
		pendingUnlocks += count;
		updateAvailableExpansions();
	}

	public int getPendingUnlocks() {
		return pendingUnlocks;
	}

	public boolean hasPendingUnlocks() {
		return pendingUnlocks > 0;
	}

	/**
	 * Return list of cells available for expansion
	 */
	public List<Point> getAvailableExpansions() {
		return new ArrayList<>(availableExpansions);
	}

	/**
	 * Check if a cell is available for expansion
	 */
	public boolean isExpansionAvailable(int x, int y) {
		return availableExpansions.stream().anyMatch(p -> p.x == x && p.y == y);
	}

	/**
	 * Unlock the cell if we have enough points.
	 * * @param x Coordinate X.
	 * @param y Coordinate Y.
	 * @return True if unlocked, false if not possible.
	 */
	public boolean unlockCell(int x, int y) {
		if (pendingUnlocks <= 0)
			return false;
		if (!isExpansionAvailable(x, y))
			return false;

		backpack.unlockCell(x, y);
		pendingUnlocks--;
		updateAvailableExpansions();
		return true;
	}


	private void updateAvailableExpansions() {
		availableExpansions.clear();

		int width = backpack.width();
		int height = backpack.height();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				if (backpack.isUnlocked(x, y))
					continue;

				if (isAdjacentToUnlocked(x, y) && isOnUnlockedPerimeter(x, y)) {
					availableExpansions.add(new Point(x, y));
				}
			}
		}
	}

	/**
	 * Check if cell is next to at least one unlocked cell
	 */
	private boolean isAdjacentToUnlocked(int x, int y) {
		int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

		for (int[] dir : directions) {
			int nx = x + dir[0];
			int ny = y + dir[1];

			if (nx >= 0 && nx < backpack.width() && ny >= 0 && ny < backpack.height() && backpack.isUnlocked(nx, ny)) {
				return true;
			}
		}
		return false;
	}

	
	private boolean isOnUnlockedPerimeter(int x, int y) {
		int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

		for (int[] dir : directions) {
			int nx = x + dir[0];
			int ny = y + dir[1];

			if (nx < 0 || nx >= backpack.width() || ny < 0 || ny >= backpack.height()) {
				return true;
			}

			if (!backpack.isUnlocked(nx, ny)) {
				return true;
			}
		}

		return false;
	}
}