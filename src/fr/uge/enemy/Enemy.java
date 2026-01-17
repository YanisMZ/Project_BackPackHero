package fr.uge.enemy;

/**
 * Represents an enemy in the game.
 * All enemies have health points, protection, and attack damage.
 */
public sealed interface Enemy permits SmallWolfRat, WolfRat {

	/**
	 * @return current health points
	 */
	int hp();

	/**
	 * @return maximum health points
	 */
	int maxHp();

	/**
	 * @return the name of the enemy
	 */
	String name();

	/**
	 * @return current protection value
	 */
	int protection();

	/**
	 * @return true if the enemy is alive
	 */
	boolean isAlive();

	/**
	 * @return attack damage dealt by the enemy
	 */
	int attackDamage();

	/**
	 * Applies damage to the enemy.
	 *
	 * @param dmg damage to apply
	 * @return the enemy after taking damage
	 */
	Enemy takeDamage(int dmg);

	/**
	 * Makes the enemy defend, increasing its protection.
	 *
	 * @return the enemy after defending
	 */
	Enemy defend();
}
