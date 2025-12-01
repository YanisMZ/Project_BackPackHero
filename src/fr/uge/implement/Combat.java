package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Combat {

    private final Hero hero;
    private final List<Enemy> enemies;
    private final Random random = new Random();

    public Combat(Hero hero, List<Enemy> enemies) {
        this.hero = hero;
        this.enemies = new ArrayList<>(enemies);
    }

    public void attackEnemy() {
        if (enemies.isEmpty()) return;

        Enemy target = enemies.get(0); // on attaque le premier ennemi
        System.out.println("⚔️ Le héros attaque " + target.getClass().getSimpleName());

        Enemy updated = target.takeDamage(25);

        if (!updated.isAlive()) {
            System.out.println("💥 Ennemi éliminé !");
            enemies.remove(target);
        } else {
            enemies.set(enemies.indexOf(target), updated);
        }
    }

    public void defendHero() {
        System.out.println("🛡️ Le héros se protège (gagne 2 protection)");
        hero.restoreMana(2);
    }

    public void enemyTurn() {
        System.out.println("\n---- Tour des ennemis ----");

        List<Enemy> updatedEnemies = new ArrayList<>();

        for (Enemy enemy : enemies) {
            int action = random.nextInt(2); // 0 = attaque, 1 = défense

            if (action == 0) {
                System.out.println(enemy.getClass().getSimpleName() + " attaque le héros !");
                hero.takeDamage(3);
            } else {
                System.out.println(enemy.getClass().getSimpleName() + " se protège !");
                enemy = enemy.defend();
            }
            updatedEnemies.add(enemy);
        }

        enemies.clear();
        enemies.addAll(updatedEnemies);

        System.out.println("Héros : hp=" + hero.hp());
    }

    public boolean isRunning() {
        return hero.hp() > 0 && !enemies.isEmpty();
    }
}
