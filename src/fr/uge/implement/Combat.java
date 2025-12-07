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

  public void attackEnemy(List<Item> items) {
    if (enemies.isEmpty())
      return;

    int damage = 1; // base
    int defense = 0;
    if (items != null) {
      for (Item it : items) {
        damage += it.attackValue();
        defense += it.defendValue();
      }
    }
    Enemy target = enemies.get(0);
    System.out
        .println("Attaque avec " + items.size() + " objets pour " + damage + " degats et se defend avec " + defense);

    Enemy updated = target.takeDamage(damage);

    if (!updated.isAlive()) {
      System.out.println("Ennemi éliminé ");
      enemies.remove(target);
    } else {
      enemies.set(0, updated);
    }
  }

  public void defendHero() {
    System.out.println("Le héros se protège (gagne 2 protection)");
    hero.restoreMana(2);
  }

  public void enemyTurn() {
    System.out.println("\n---- Tour des ennemis ----");

    for (Enemy enemy : enemies) {
      int action = random.nextInt(2); // 0 = attack, 1 = defense

      if (action == 0) {
        System.out.println(enemy.name() + " attaque le héros !");
        hero.takeDamage(3);
      } else {
        System.out.println(enemy.name() + " se protège !");
        enemy = enemy.defend();
      }

    }

    System.out.println("Héros : hp=" + hero.hp());
  }

  public boolean isRunning() {
    return hero.hp() > 0 && !enemies.isEmpty();
  }
}
