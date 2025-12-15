package fr.uge.implement;

public class Hero {
	private final int maxHp;
	private final int maxStamina;
	private int hp;
	private int currentStamina;
	private int protection;
	

	public Hero(int maxHp, int protection, int maxStamina) {
		this.maxHp = maxHp;
		this.maxStamina = maxStamina;
		this.hp = maxHp;
		this.currentStamina = maxStamina;
		this.protection = protection;
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

	/* ===================== HP ===================== */

	public void takeDamage(int damage) {
		int actualDamage = Math.max(0, damage - protection);
		hp = Math.max(0, hp - actualDamage);

		System.out.println("Héros prend " + actualDamage + " dégâts (Protection: " + protection + ")");
		System.out.println("HP: " + hp + "/" + maxHp);
	}

	public void addProtection(int value) {
		protection += value;
		System.out.println("Protection totale: " + protection);
	}

	public void resetProtection() {
		protection = 0;
	}

	/* ===================== STAMINA ===================== */

	/**
	 * Vérifie si le héros a assez de stamina
	 */
	public boolean hasStamina(int cost) {
		return currentStamina >= cost;
	}

	/**
	 * Utilise de la stamina
	 */
	public void useStamina(int cost) {
		if (cost > currentStamina) {
			throw new IllegalStateException("Pas assez de stamina !");
		}
		currentStamina -= cost;
	}

	/**
	 * Réinitialise la stamina au début d'un nouveau tour
	 */
	public void resetStaminaForNewTurn() {
		currentStamina = maxStamina;
	}

	public boolean isAlive() {
		return hp > 0;
	}

	public int HeroMaxHp() {
		// TODO Auto-generated method stub
		return 40;
	}

	public int mana() {
		// TODO Auto-generated method stub
		return 5;
	}
}