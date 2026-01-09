package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Battle {

    private final Hero hero;
    private final BackPack backpack;
    private final List<Enemy> enemies = new ArrayList<>();
    private final Random random = new Random();

    /** Actions possibles des ennemis */
    public enum EnemyAction {
        ATTACK, DEFEND, MALEDICTION
    }

    private final List<EnemyAction> enemyActions = new ArrayList<>();
    private boolean playerTurnActive = true;
    private int defeatedEnemiesThisCombat = 0;

    public Battle(Hero hero, BackPack backpack) {
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

        playerTurnActive = true;
        hero.resetStaminaForNewTurn();

        announceEnemyTurn();
    }

    /* ===================== PLAYER ===================== */

    public boolean useItem(Item item) {
        if (!playerTurnActive) {
            System.out.println("Ce n'est pas votre tour !");
            return false;
        }
        if (!hero.hasStamina(item.staminaCost())) {
            System.out.println("Pas assez de stamina !");
            return false;
        }

        hero.useStamina(item.staminaCost());

        if (item.staminaRegen() > 0) {
            hero.addStamina(item.staminaRegen());
            System.out.println("Energie + " + item.staminaRegen());
        }

        if (item.healthRegen() > 0) {
            hero.heal(item.healthRegen());
            System.out.println("Soins : +" + item.healthRegen() + " HP");
        }

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

        Item updatedItem = item.decreaseDurability();
        if (updatedItem.isBroken()) {
            backpack.updateItem(item, null);
            System.out.println(item.name() + " a été consommé.");
        } else {
            backpack.updateItem(item, updatedItem);
        }

        return true;
    }

    public void endPlayerTurn() {
        if (!playerTurnActive) {
            System.out.println("Le tour joueur est déjà terminé !");
            return;
        }

        playerTurnActive = false;
        System.out.println("\n========== FIN DU TOUR JOUEUR ==========");
        System.out.println("Stamina utilisée ce tour: " + (hero.maxStamina() - hero.currentStamina()) + "/" + hero.maxStamina());

        executeEnemyTurn();
    }

    /* ===================== ENEMIES ===================== */

    /** Annonce les actions ennemies pour le tour suivant */
    public void announceEnemyTurn() {
        enemyActions.clear();
        System.out.println("\n========== ANNONCE DES ENNEMIS ==========");

        for (Enemy e : enemies) {
            int roll = random.nextInt(100); // 0 à 99
            EnemyAction action;

            if (roll < 45) {            // 45% ATTACK
                action = EnemyAction.ATTACK;
            } else if (roll < 90) {     // 45% DEFEND
                action = EnemyAction.DEFEND;
            } else {                     // 10% MALEDICTION
                action = EnemyAction.MALEDICTION;
            }

            enemyActions.add(action);

            switch (action) {
                case ATTACK -> System.out.println("⚔️  " + e.name() + " va ATTAQUER (dégâts: " + e.attackDamage() + ")");
                case DEFEND -> System.out.println("🛡️  " + e.name() + " va SE DÉFENDRE");
                case MALEDICTION -> System.out.println("☠️  " + e.name() + " va LANCER UNE MALÉDICTION !");
            }
        }

        System.out.println("=========================================\n");
    }
    
    
    public Malediction chooseMalediction() {
      if (random.nextBoolean()) {
          return Malediction.formeS();
      } else {
          return Malediction.carre();
      }
  }


    /** Exécute le tour ennemi pour ATTACK et DEFEND. MALEDICTION est gérée par GameController */
    public void executeEnemyTurn() {
        if (enemyActions.isEmpty()) {
            System.out.println("Les ennemis n'ont pas encore annoncé leurs actions !");
            return;
        }

        System.out.println("\n========== EXÉCUTION DU TOUR ENNEMI ==========");
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            EnemyAction action = enemyActions.get(i);

            switch (action) {
                case ATTACK -> {
                    int damage = e.attackDamage();
                    System.out.println(e.name() + " attaque ! Dégâts: " + damage);
                    hero.takeDamage(damage);
                    System.out.println("HP du héros: " + hero.hp() + "/" + hero.maxHp());
                }
                case DEFEND -> {
                    System.out.println(e.name() + " se défend !");
                    enemies.set(i, e.defend());
                }
                case MALEDICTION -> {
                    System.out.println(e.name() + " tente de lancer une malédiction ! (GameController gère)");
                }
            }
        }

        hero.resetProtection();

        if (isRunning()) {
            startNewPlayerTurn();
        }

        System.out.println("==============================================\n");
    }

    private void startNewPlayerTurn() {
        playerTurnActive = true;
        hero.resetStaminaForNewTurn();
        System.out.println("\n========== NOUVEAU TOUR JOUEUR ==========");
        System.out.println("Stamina disponible: " + hero.currentStamina() + "/" + hero.maxStamina());
        System.out.println("=========================================\n");

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
        System.out.println("[Battle] Retourne : " + defeatedEnemiesThisCombat);
        return defeatedEnemiesThisCombat;
    }
}
