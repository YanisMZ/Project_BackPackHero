package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Combat {

	private final Hero hero;
	private final List<Enemy> enemies;
	private final Random random = new Random();

	public Combat(Hero hero) {
		this.hero = hero;
		this.enemies = new ArrayList<>();
	}

	public void initEnemies() {

		var nb_enemies = random.nextInt(3) + 1;
		System.out.println("le nombre " + nb_enemies);
		for (int i = 0; i < nb_enemies; i++) {
			var type = random.nextBoolean();
			if (type)
				enemies.add(new SmallWolfRat());
			else
				enemies.add(new WolfRat());
		}
		
	
	}
	
	public int nbEnemy() {
		return enemies.size();
	}

	public void attackEnemy() {
		if (enemies.isEmpty())
			return;

		Enemy target = enemies.getFirst(); // on attaque le premier ennemi
		System.out.println("⚔️ Le héros attaque " + target.getClass().getSimpleName());

		Enemy updated = target.takeDamage(25);

		if (!updated.isAlive()) {
			System.out.println("💥 Ennemi éliminé !");
			enemies.remove(target);
		} else {
			enemies.set(0, updated);
		}
	}

	public void defendHero() {
		System.out.println("🛡️ Le héros se protège (gagne 2 protection)");
		hero.restoreMana(2);
	}

	public void enemyTurn() {
		System.out.println("\n---- Tour des ennemis ----");


		for (Enemy enemy : enemies) {
			int action = random.nextInt(2); // 0 = attaque, 1 = défense

			if (action == 0) {
				System.out.println(enemy.getClass().getSimpleName() + " attaque le héros !");
				hero.takeDamage(3);
			} else {
				System.out.println(enemy.getClass().getSimpleName() + " se protège !");
				enemy = enemy.defend();
			}
			
		}

		

		System.out.println("Héros : hp=" + hero.hp());
	}

	public boolean isRunning() {
		return hero.hp() > 0 && !enemies.isEmpty();
	}
}
