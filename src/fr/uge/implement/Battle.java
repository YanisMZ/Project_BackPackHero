package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Battle {

	private final Hero hero;
	private final BackPack backpack;
	private final List<Enemy> enemies = new ArrayList<>();
	private final Random random = new Random();

	private enum EnemyAction {
		ATTACK, DEFEND
	}

	private final List<EnemyAction> enemyActions = new ArrayList<>();
	private boolean playerTurnActive = true;
	private int defeatedEnemiesThisCombat = 0;

	public Battle(Hero hero,BackPack backpack) {
		this.hero = hero;
		this.backpack = backpack;
	}

	/* ===================== INIT ===================== */

	public void initEnemies() {
		enemies.clear();
		int nb = random.nextInt(3) + 1;

		for (int i = 0; i < nb; i++) {
			enemies.add(random.nextBoolean() ? new SmallWolfRat() : new WolfRat());
		}

		// Au début du combat, le tour joueur est actif
		playerTurnActive = true;
		hero.resetStaminaForNewTurn();

		// Les ennemis annoncent immédiatement leurs actions pour le premier tour
		announceEnemyTurn();
	}

	/* ===================== PLAYER ===================== */

	/**
	 * Le joueur utilise un item. Cela consomme de la stamina. Le joueur peut
	 * utiliser plusieurs items tant qu'il a de la stamina.
	 * 
	 * @return true si l'item a été utilisé avec succès
	 */
	public boolean useItem(Item item) {
    if (!playerTurnActive) {
        System.out.println("Ce n'est pas votre tour !");
        return false;
    }

    if (!hero.hasStamina(item.staminaCost())) {
        System.out.println("Pas assez de stamina !");
        return false;
    }

    // 1. Consommation et gain d'énergie
    hero.useStamina(item.staminaCost());
    
    if (item.staminaRegen() > 0) {
        hero.addStamina(item.staminaRegen());
        System.out.println("Energie + " + item.staminaRegen());
    }

    // --- NOUVEAU : Gestion du Soin ---
    if (item.healthRegen() > 0) {
        hero.heal(item.healthRegen()); // Assure-toi d'avoir une méthode heal(int) dans Hero
        System.out.println("Soins : +" + item.healthRegen() + " HP");
    }

    // 2. Défense et Attaque
    if (item.defendValue() > 0) {
        hero.addProtection(item.defendValue());
    }

    if (!enemies.isEmpty() && item.attackValue() > 0) {
        Enemy target = enemies.get(0);
        target = target.takeDamage(item.attackValue());
        if (!target.isAlive()) {
            defeatedEnemiesThisCombat++;
            enemies.remove(0);
        } else {
            enemies.set(0, target);
        }
    }

    // 3. Gestion de l'immuabilité et mise à jour du sac à dos
    Item updatedItem = item.decreaseDurability();
    
    if (updatedItem.isBroken()) {
        backpack.updateItem(item, null); 
        System.out.println(item.name() + " a été consommé.");
    } else {
        backpack.updateItem(item, updatedItem);
    }

    return true;
}

	/**
	 * Le joueur met fin à son tour. Cela déclenche l'exécution immédiate du tour
	 * ennemi.
	 */
	public void endPlayerTurn() {
		if (!playerTurnActive) {
			System.out.println("Le tour joueur est déjà terminé !");
			return;
		}

		playerTurnActive = false;
		System.out.println("\n========== FIN DU TOUR JOUEUR ==========");
		System.out
				.println("Stamina utilisée ce tour: " + (hero.maxStamina() - hero.currentStamina()) + "/" + hero.maxStamina());

		// Exécuter immédiatement le tour des ennemis
		executeEnemyTurn();
	}

	/* ===================== ENEMIES ===================== */

	/**
	 * Les ennemis annoncent leurs actions pour le prochain tour. Cela est appelé
	 * AVANT que le joueur ne commence son tour.
	 */
	public void announceEnemyTurn() {
		enemyActions.clear();
		System.out.println("\n========== ANNONCE DES ENNEMIS ==========");

		for (Enemy e : enemies) {
			EnemyAction action = random.nextBoolean() ? EnemyAction.ATTACK : EnemyAction.DEFEND;
			enemyActions.add(action);

			if (action == EnemyAction.ATTACK) {
				System.out.println("⚔️  " + e.name() + " va ATTAQUER (dégâts: " + e.attackDamage() + ")");
			} else {
				System.out.println("🛡️  " + e.name() + " va SE DÉFENDRE");
			}
		}

		System.out.println("=========================================\n");
	}

	/**
	 * Exécute les actions annoncées des ennemis. Après cela, un nouveau tour joueur
	 * commence.
	 */
	public void executeEnemyTurn() {
		if (enemyActions.isEmpty()) {
			System.out.println("Les ennemis n'ont pas encore annoncé leurs actions !");
			return;
		}

		System.out.println("\n========== EXÉCUTION DU TOUR ENNEMI ==========");

		for (int i = 0; i < enemies.size(); i++) {
			Enemy e = enemies.get(i);
			EnemyAction action = enemyActions.get(i);

			if (action == EnemyAction.ATTACK) {
				int damage = e.attackDamage();
				System.out.println(e.name() + " attaque ! Dégâts: " + damage);
				hero.takeDamage(damage);
				System.out.println("HP du héros: " + hero.hp() + "/" + hero.maxHp());
			} else {
				System.out.println(e.name() + " se défend !");
				enemies.set(i, e.defend());
			}
		}

		// Réinitialiser la protection du héros après le tour ennemi
		hero.resetProtection();

		// Commencer un nouveau tour joueur
		if (isRunning()) {
			startNewPlayerTurn();
		}

		System.out.println("==============================================\n");
	}

	/**
	 * Démarre un nouveau tour pour le joueur. Les ennemis annoncent leurs actions
	 * AVANT que le joueur ne joue.
	 */
	private void startNewPlayerTurn() {
		playerTurnActive = true;
		hero.resetStaminaForNewTurn();
		System.out.println("\n========== NOUVEAU TOUR JOUEUR ==========");
		System.out.println("Stamina disponible: " + hero.currentStamina() + "/" + hero.maxStamina());
		System.out.println("=========================================\n");

		// Les ennemis annoncent leurs actions pour ce tour
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

	public Hero getHero() {
		return hero;
	}

	public int nbEnemy() {
		// TODO Auto-generated method stub
		return enemies.size();
	}

	/**
	 * Retourne le nombre d'ennemis vaincus dans ce combat
	 */
	public int getDefeatedEnemiesCount() {
		 System.out.println("[Battle] Retourne : " + defeatedEnemiesThisCombat);
	    return defeatedEnemiesThisCombat;
	}

}