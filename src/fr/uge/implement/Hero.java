package fr.uge.implement;

public class Hero {
	private final int maxHp;
	private final int maxStamina;
	private int hp;
	private int currentStamina;
	private int protection;
	private final BackPack backpack;

	public Hero(int maxHp, int protection, int maxStamina, BackPack backpack) {
		this.maxHp = maxHp;
		this.maxStamina = maxStamina;
		this.hp = maxHp;
		this.currentStamina = maxStamina;
		this.protection = protection;
		this.backpack = backpack;
	}

	/* ===================== GETTERS ===================== */
	public int maxHp() {
		return maxHp;
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

	
}
