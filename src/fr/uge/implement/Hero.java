package fr.uge.implement;

public class Hero {
	private int hp;
	private int mana;
	private int shieldProtection = 0;
	private int maxHp = 40;

	public Hero(int hp, int mana) {
		this.hp = hp;
		this.mana = mana;
	}

	public int hp() {
		return hp;
	}

	public int mana() {
		return mana;
	}

	/**
	 * @param amount
	 */
	public void takeDamage(int amount) {
    if (shieldProtection > 0) {
        int used = Math.min(amount, shieldProtection);
        shieldProtection -= used;
        amount -= used;
        System.out.println("Le héros utilise " + used + " points de protection. Protection restante : " + shieldProtection);
    }

    hp = Math.max(0, hp - amount);
    System.out.println("Le héros subit " + amount + " dégâts. (hp = " + hp + ")");
}


	public int shieldProtection() {
		return shieldProtection;
	}

	public void addProtection(int amount) {
		shieldProtection += amount;
	}
	
	 public void resetProtection() {
	    shieldProtection = 0;
	  }

	public void restoreMana(int amount) {
		mana += amount;
	}

	@Override
	public String toString() {
		return "Hero(hp=" + hp + ", mana=" + mana + ")";
	}
	
	public int HeroMaxHp() {
		return maxHp;
	}
}
