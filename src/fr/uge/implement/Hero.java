package fr.uge.implement;

public class Hero {
	private final int maxHp;
	private final int maxStamina;
	private int hp;
	private int currentStamina;
	private int protection;
	private float experience;
	private float maxExperience = 100;
	private final BackPack backpack;

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
	public int maxHp() {
		return maxHp;
	}

	public float exp() {
		return experience;
	}

	public int hp() {
		return hp;
	}

	public int maxStamina() {
		return maxStamina;
	}

	public int currentStamina() {
		return currentStamina;
	}

	public int protection() {
		return protection;
	}

	public boolean hasEnoughGold(int price) {
		return gold() >= price;
	}

	public int gold() {
		return backpack.getQuantity("Gold");
	}

	/* ===================== OR ===================== */

	public void addGold(int amount) {
		backpack.addQuantity("Gold", amount);
	}

	public void removeGold(int amount) {
		backpack.removeQuantity("Gold", amount);
	}

	/* ===================== HP ===================== */
	public void takeDamage(int damage) {
		int actualDamage = Math.max(0, damage - protection);
		hp = Math.max(0, hp - actualDamage);
		System.out.println("Héros prend " + actualDamage + " dégâts (Protection: " + protection + ")");
		System.out.println("HP: " + hp + "/" + maxHp);
	}

	public void addProtection(int value) {
		protection += value;
	}

	public void resetProtection() {
		protection = 0;
	}

	/* ===================== STAMINA ===================== */

	public void addStamina(int amount) {
		this.currentStamina = Math.min(this.maxStamina, this.currentStamina + amount);
	}

	public boolean hasStamina(int cost) {
		return currentStamina >= cost;
	}

	public void useStamina(int cost) {
		if (cost > currentStamina)
			throw new IllegalStateException("Pas assez de stamina !");
		currentStamina -= cost;
	}

	public void resetStaminaForNewTurn() {
		currentStamina = maxStamina;
	}

	public boolean isAlive() {
		return hp > 0;
	}

	/* ===================== EXP ===================== */
	public float maxExp() {
		return maxExperience;
	}

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

	public void addExp(double amount) {
		this.experience += amount;
		if (this.experience > this.maxExp()) {
			this.experience = this.maxExp();
		}
	}

	public int increaseExp(int exp, int enemyHp) {
		return exp + (8 / 10) * enemyHp;
	}

	/* ===================== STUBS ===================== */
	public int HeroMaxHp() {
		return 40;
	}

	public int mana() {
		return 5;
	}

	public BackPack getBackpack() {
		return backpack;
	}

	public void heal(int amount) {
		if (amount < 0) {
			return; // On ne soigne pas des montants négatifs
		}

		// On ajoute le soin tout en s'assurant de ne pas dépasser maxHp
		this.hp = Math.min(this.hp + amount, this.maxHp);

		System.out.println("Héros soigné ! HP actuels : " + this.hp + "/" + this.maxHp);
	}

}
