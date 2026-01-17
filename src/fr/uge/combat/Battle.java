package fr.uge.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.uge.backpack.BackPack;
import fr.uge.enemy.Enemy;
import fr.uge.enemy.Hero;
import fr.uge.enemy.SmallWolfRat;
import fr.uge.enemy.WolfRat;
import fr.uge.items.Item;
import fr.uge.items.Malediction;

public class Battle {

	private final Hero hero;
	private final BackPack backpack;
	private final CombatEffects combatEffects;
	private final List<Enemy> enemies = new ArrayList<>();
	private final Random random;

	public enum EnemyAction {
		ATTACK, DEFEND, MALEDICTION
	}

	private final List<EnemyAction> enemyActions = new ArrayList<>();
	private boolean playerTurnActive = true;
	private int defeatedEnemiesThisCombat = 0;

	public Battle(Hero hero, BackPack backpack, Random random) {
		this.random = random;
		this.hero = hero;
		this.backpack = backpack;
		this.combatEffects = new CombatEffects(backpack, hero);
	}

	/* ===================== INIT ===================== */

	public void initEnemies() {
		enemies.clear();
		int nb = random.nextInt(3) + 1;
		for (int i = 0; i < nb; i++) {
			enemies.add(random.nextBoolean() ? new SmallWolfRat() : new WolfRat());
		}

		playerTurnActive = true;
		hero.resetStaminaForNewTurn();

		announceEnemyTurn();
	}

	/* ===================== PLAYER ===================== */
	
	/**
	 * Player use an item to attack or heal.
	 * * @param item The item we want to use.
	 * @return True if item is used, false if issue.
	 */
	public boolean useItem(Item item) {
		if (!playerTurnActive) {
			return false;
		}
		if (!hero.hasStamina(item.staminaCost())) {
			return false;
		}

		hero.useStamina(item.staminaCost());

		// stamina
		if (item.staminaRegen() > 0) {
			hero.addStamina(item.staminaRegen());
		}

		// heal
		if (item.healthRegen() > 0) {
			hero.heal(item.healthRegen());
		}

		// Défense
		if (item.defendValue() > 0) {
			hero.addProtection(item.defendValue());
		}

		if (!enemies.isEmpty() && item.attackValue() > 0) {

			int[] position = findItemPosition(item);

			CombatResult result = combatEffects.applyItemEffects(item, position[0], position[1], hero);

			int finalDamage = result.damage();

			Enemy target = enemies.get(0);
			target = target.takeDamage(finalDamage);

			if (!target.isAlive()) {
				defeatedEnemiesThisCombat++;
				enemies.remove(0);
			} else {
				enemies.set(0, target);
			}
		}

		Item updatedItem = item.decreaseDurability();
		if (updatedItem.isBroken()) {
			backpack.updateItem(item, null);
		} else {
			backpack.updateItem(item, updatedItem);
		}

		return true;
	}

	/**
	 * Find an item position inside the backpack
	 * * @param item  item that we want to find
	 * @return two int that show the coordinate x, y of the item
	 */
	private int[] findItemPosition(Item item) {
		Item[][] grid = backpack.grid();
		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				if (grid[y][x] == item) {
					return new int[] { x, y };
				}
			}
		}
		return new int[] { 0, 0 };
	}

	public void endPlayerTurn() {
		if (!playerTurnActive) {
			return;
		}

		playerTurnActive = false;

		executeEnemyTurn();
	}

	/* ===================== ENEMIES ===================== */

	public void announceEnemyTurn() {
		enemyActions.clear();

		for (Enemy e : enemies) {
			int roll = random.nextInt(100);
			EnemyAction action;

			if (roll < 45) {
				action = EnemyAction.ATTACK;
			} else if (roll < 90) {
				action = EnemyAction.DEFEND;
			} else {
				action = EnemyAction.MALEDICTION;
			}

			enemyActions.add(action);

		}

	}

	public Malediction chooseMalediction() {
		if (random.nextBoolean()) {
			return Malediction.formeS();
		} else {
			return Malediction.carre();
		}
	}

	public void executeEnemyTurn() {
		if (enemyActions.isEmpty()) {
			return;
		}

		for (int i = 0; i < enemies.size(); i++) {
			Enemy e = enemies.get(i);
			EnemyAction action = enemyActions.get(i);

			switch (action) {
			case ATTACK -> {
				int damage = e.attackDamage();
				hero.takeDamage(damage);
			}
			case DEFEND -> {
				enemies.set(i, e.defend());
			}
			case MALEDICTION -> {
			}
			}
		}

		hero.resetProtection();

		if (isRunning()) {
			startNewPlayerTurn();
		}

	}

	public void startNewPlayerTurn() {
		playerTurnActive = true;
		hero.resetStaminaForNewTurn();

		announceEnemyTurn();
	}

	/* ===================== STATE ===================== */

	public boolean isRunning() {
		return hero.hp() > 0 && !enemies.isEmpty();
	}

	public boolean isPlayerTurnActive() {
		return playerTurnActive;
	}

	public List<Enemy> getEnemy() {
		return List.copyOf(enemies);
	}

	public List<EnemyAction> getEnemyActions() {
		return List.copyOf(enemyActions);
	}

	public Hero getHero() {
		return hero;
	}

	public int nbEnemy() {
		return enemies.size();
	}

	public int getDefeatedEnemiesCount() {
		return defeatedEnemiesThisCombat;
	}
}