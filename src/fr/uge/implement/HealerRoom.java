package fr.uge.implement;
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
            if (!hero.hasEnoughGold(HEAL_COST)) {
                System.out.println("❌ Pas assez d'or ! (" + HEAL_COST + " requis)");
            } else {
                System.out.println("❤️ Vous êtes déjà à HP max !");
            }
            return false;
        }
        
        hero.removeGold(HEAL_COST);
        hero.heal(HEAL_AMOUNT);
        System.out.println("✅ Soigné de " + HEAL_AMOUNT + " HP pour " + HEAL_COST + " or !");
        return true;
    }
}