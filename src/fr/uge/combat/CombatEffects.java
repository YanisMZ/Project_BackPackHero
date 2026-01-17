package fr.uge.combat;

import java.util.HashSet;
import java.util.Set;

import fr.uge.backpack.BackPack;
import fr.uge.enemy.Hero;
import fr.uge.items.Item;

/**
 * System to calculate effect during fight + iteraction betwenn elements
 */
public class CombatEffects {

	private final BackPack backpack;
	private final Hero hero;

	public CombatEffects(BackPack backpack, Hero hero) {
		this.backpack = backpack;
		this.hero = hero;
	}

	/**
	 * Calculate real damage of weapon.
	 */
	public int calculateDamage(Item weapon, int x, int y) {
		int baseDamage = weapon.attackValue();

		if (weapon.name().equalsIgnoreCase("Hachette")) {
			if (hero.protection() > 0) {
				return 1;
			} else {
				return baseDamage;
			}
		}

		return baseDamage;
	}

	/**
	 * Calculate bonus life if weapon is used.
	 */
	public int calculateLifeBonus(Item weapon, int x, int y) {
		int lifeBonus = 0;

		if (isAdjacentToHeartGem(x, y)) {
			lifeBonus += 1;
		}

		return lifeBonus;
	}

	/**
	 * Check if the position is near a heart gem.
	 */
	private boolean isAdjacentToHeartGem(int x, int y) {
		Item item = backpack.grid()[y][x];
		if (item == null)
			return false;

		Set<int[]> itemPositions = getItemPositions(item);

		for (int[] pos : itemPositions) {
			if (hasHeartGemAround(pos[0], pos[1])) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Cehck if cell is near a gem
	 */
	private boolean hasHeartGemAround(int x, int y) {
		int[][] directions = { { -1, -1 }, { 0, -1 }, { 1, -1 }, { -1, 0 }, { 1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };

		for (int[] dir : directions) {
			int newX = x + dir[0];
			int newY = y + dir[1];

			if (newX >= 0 && newX < backpack.width() && newY >= 0 && newY < backpack.height()) {

				Item neighbor = backpack.grid()[newY][newX];
				if (neighbor != null && isHeartGem(neighbor)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Get all positions that are occupied by all items
	 */
	private Set<int[]> getItemPositions(Item item) {
		Set<int[]> positions = new HashSet<>();
		Item[][] grid = backpack.grid();

		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				if (grid[y][x] == item) {
					positions.add(new int[] { x, y });
				}
			}
		}

		return positions;
	}

	/**
	 * Check if an item is a heart gem
	 */
	private boolean isHeartGem(Item item) {
		return item.name().equalsIgnoreCase("Heal");
	}

	/**
	 * Apply all effects when item is used.
	 * Calculate damage and heal.
	 * * @param item The weapon used.
	 * @param x Position x in bag.
	 * @param y Position y in bag.
	 * @param hero The player.
	 * @return The result with damage and heal amount.
	 */
	public CombatResult applyItemEffects(Item item, int x, int y, Hero hero) {
		int damage = calculateDamage(item, x, y);
		int lifeBonus = calculateLifeBonus(item, x, y);

		if (lifeBonus > 0) {
			hero.heal(lifeBonus);
		}

		return new CombatResult(damage, lifeBonus);
	}
}