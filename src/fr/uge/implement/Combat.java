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

    public void start() {
        System.out.println("=== Début du combat ===");
        System.out.println("Héros : " + hero);
        System.out.println("Ennemis : " + enemies + "\n");

        while (hero.hp() > 0 && !enemies.isEmpty()) {
            heroTurn();
            if (enemies.isEmpty()) break;     // Si héros tue tout → fin

            enemyTurn();
        }

        if (hero.hp() <= 0) {
            System.out.println("💀 Le héros est mort !");
        } else {
            System.out.println("🎉 Le héros a vaincu tous les ennemis !");
        }
    }

    // ============================
    //      TOUR DU HÉROS
    // ============================
    private void heroTurn() {
        System.out.println("\n---- Tour du héros ----");

        int action = random.nextInt(2); // 0 = attaquer, 1 = se défendre

        if (action == 0) {
            attackEnemy();
        } else {
            defendHero();
        }
    }

    private void attackEnemy() {
        Enemy target = enemies.get(0); // on attaque le premier ennemi
        System.out.println("⚔️ Le héros attaque " + target.getClass().getSimpleName());

        // dégâts fixes pour l'instant : 5
        Enemy updated = target.takeDamage(5);

        if (!updated.isAlive()) {
            System.out.println("💥 Ennemi éliminé !");
            enemies.remove(target);
        } else {
            enemies.set(enemies.indexOf(target), updated);
        }
    }

    private void defendHero() {
        System.out.println("🛡️ Le héros se protège (gagne 2 protection)");
        hero.restoreMana(2);  // on utilise mana comme "protection"
    }

    // ============================
    //      TOUR DES ENNEMIS
    // ============================
    private void enemyTurn() {
        System.out.println("\n---- Tour des ennemis ----");

        List<Enemy> updatedEnemies = new ArrayList<>();

        for (Enemy enemy : enemies) {
            int action = random.nextInt(2);  // 0 = attaque, 1 = défense

            if (action == 0) {
                System.out.println(enemy.getClass().getSimpleName() + " attaque le héros !");
                hero.takeDamage(3); // dégâts simples pour l'instant
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
}
