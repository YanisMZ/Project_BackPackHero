package fr.uge.combat;

/**
 * Result of a fight action
 * 
 * @param damage    Damage done
 * @param lifeBonus Health points recovered
 */
public record CombatResult(int damage, int lifeBonus) {
	public CombatResult {
		if (damage < 0) {
			throw new IllegalArgumentException("Les dégâts ne peuvent pas être négatifs");
		}
		if (lifeBonus < 0) {
			throw new IllegalArgumentException("Le bonus de vie ne peut pas être négatif");
		}
	}


	public CombatResult(int damage) {
		this(damage, 0);
	}

	/**
	 * Check if result has bonus HP
	 */
	public boolean hasLifeBonus() {
		return lifeBonus > 0;
	}

	/**
	 * check if damage has been apply
	 */
	public boolean hasDamage() {
		return damage > 0;
	}
}