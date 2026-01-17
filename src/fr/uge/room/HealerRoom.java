package fr.uge.room;

import fr.uge.enemy.Hero;

public class HealerRoom {
	private static final int HEAL_AMOUNT = 10;
	private static final int HEAL_COST = 5;

	public HealerRoom() {
	}

	public int getHealAmount() {
		return HEAL_AMOUNT;
	}

	public int getHealCost() {
		return HEAL_COST;
	}

	public boolean canHeal(Hero hero) {
		return hero.hasEnoughGold(HEAL_COST) && hero.hp() < hero.maxHp();
	}

	public boolean healHero(Hero hero) {
		if (!canHeal(hero)) {
			return false;
		}

		hero.removeGold(HEAL_COST);
		hero.heal(HEAL_AMOUNT);

		return true;
	}
}