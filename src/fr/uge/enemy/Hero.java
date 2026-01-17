package fr.uge.enemy;

import fr.uge.backpack.BackPack;

/**
 * Represents the hero controlled by the player.
 * The hero has health, stamina, protection, experience, and a backpack.
 */
public class Hero {

	private final int maxHp;
	private final int maxStamina;
	private int hp;
	private int currentStamina;
	private int protection;
	private float experience;
	private float maxExperience = 100;
	private final BackPack backpack;

	/**
	 * Creates a new hero.
	 *
	 * @param maxHp        maximum health points
	 * @param protection   initial protection value
	 * @param maxStamina   maximum stamina
	 * @param exp          initial experience
	 * @param backpack     hero backpack
	 */
	public Hero(int maxHp, int protection, int maxStamina, float exp, BackPack backpack) {
		this.maxHp = maxHp;
		this.maxStamina = maxStamina;
		this.hp = maxHp;
		this.currentStamina = maxStamina;
		this.protection = protection;
		this.experience = exp;
		this.backpack = backpack;
	}

	/* ===================== GETTERS ===================== */

	/** @return maximum health points */
	public int maxHp() {
		return maxHp;
	}

	/** @return current experience */
	public float exp() {
		return experience;
	}

	/** @return current health points */
	public int hp() {
		return hp;
	}

	/** @return maximum stamina */
	public int maxStamina() {
		return maxStamina;
	}

	/** @return current stamina */
	public int currentStamina() {
		return currentStamina;
	}

	/** @return current protection value */
	public int protection() {
		return protection;
	}

	/**
	 * Checks if the hero has enough gold.
	 *
	 * @param price required amount
	 * @return true if the hero has enough gold
	 */
	public boolean hasEnoughGold(int price) {
		return gold() >= price;
	}

	/**
	 * @return amount of gold in the backpack
	 */
	public int gold() {
		return backpack.getQuantity("Gold");
	}

	/* ===================== GOLD ===================== */

	/**
	 * Adds gold to the hero.
	 *
	 * @param amount amount of gold to add
	 */
	public void addGold(int amount) {
		backpack.addQuantity("Gold", amount);
	}

	/**
	 * Removes gold from the hero.
	 *
	 * @param amount amount of gold to remove
	 */
	public void removeGold(int amount) {
		backpack.removeQuantity("Gold", amount);
	}

	/* ===================== HP ===================== */

	/**
	 * Applies damage to the hero, reduced by protection.
	 *
	 * @param damage incoming damage
	 */
	public void takeDamage(int damage) {
		int actualDamage = Math.max(0, damage - protection);
		hp = Math.max(0, hp - actualDamage);
	}

	/**
	 * Adds protection to the hero.
	 *
	 * @param value protection to add
	 */
	public void addProtection(int value) {
		protection += value;
	}

	/**
	 * Resets protection to zero.
	 */
	public void resetProtection() {
		protection = 0;
	}

	/* ===================== STAMINA ===================== */

	/**
	 * Restores stamina.
	 *
	 * @param amount stamina to restore
	 */
	public void addStamina(int amount) {
		this.currentStamina = Math.min(this.maxStamina, this.currentStamina + amount);
	}

	/**
	 * Checks if the hero has enough stamina.
	 *
	 * @param cost stamina cost
	 * @return true if enough stamina is available
	 */
	public boolean hasStamina(int cost) {
		return currentStamina >= cost;
	}

	/**
	 * Uses stamina.
	 *
	 * @param cost stamina cost
	 * @throws IllegalStateException if not enough stamina
	 */
	public void useStamina(int cost) {
		if (cost > currentStamina)
			throw new IllegalStateException("Not enough stamina!");
		currentStamina -= cost;
	}

	/**
	 * Resets stamina at the beginning of a new turn.
	 */
	public void resetStaminaForNewTurn() {
		currentStamina = maxStamina;
	}

	/**
	 * @return true if the hero is alive
	 */
	public boolean isAlive() {
		return hp > 0;
	}

	/* ===================== EXPERIENCE ===================== */

	/** @return maximum experience */
	public float maxExp() {
		return maxExperience;
	}

	/**
	 * Returns the level based on experience.
	 *
	 * @param exp current experience
	 * @return hero level (1 to 5)
	 */
	public int lvl(float exp) {
		if (exp < (1 / 10.0f) * maxExp()) {
			return 1;
		} else if (exp < (3 / 10.0f) * maxExp()) {
			return 2;
		} else if (exp < (3 / 5.0f) * maxExp()) {
			return 3;
		} else if (exp < (9 / 10.0f) * maxExp()) {
			return 4;
		} else {
			return 5;
		}
	}

	/**
	 * Returns the required experience for a given level.
	 *
	 * @param level hero level
	 * @return experience needed
	 */
	public float getXpForLevel(int level) {
		float max = maxExp();

		if (level <= 1)
			return 0;
		if (level == 2)
			return (1.0f / 10.0f) * max;
		if (level == 3)
			return (3.0f / 10.0f) * max;
		if (level == 4)
			return (3.0f / 5.0f) * max;
		if (level == 5)
			return (9.0f / 10.0f) * max;

		return max;
	}

	/**
	 * Adds experience to the hero.
	 *
	 * @param amount experience to add
	 */
	public void addExp(double amount) {
		this.experience += amount;
		if (this.experience > this.maxExp()) {
			this.experience = this.maxExp();
		}
	}

	/**
	 * Calculates experience gained from an enemy.
	 *
	 * @param exp     current experience
	 * @param enemyHp enemy health points
	 * @return new experience value
	 */
	public int increaseExp(int exp, int enemyHp) {
		return exp + (8 / 10) * enemyHp;
	}

	/* ===================== STUBS ===================== */

	/** @return default hero maximum HP */
	public int HeroMaxHp() {
		return 40;
	}

	/** @return hero mana (not fully implemented) */
	public int mana() {
		return 5;
	}

	/** @return hero backpack */
	public BackPack getBackpack() {
		return backpack;
	}

	/**
	 * Heals the hero.
	 *
	 * @param amount healing amount
	 */
	public void heal(int amount) {
		if (amount < 0) {
			return;
		}
		this.hp = Math.min(this.hp + amount, this.maxHp);
	}
}
